package joist.codegen.passes;

import java.util.ArrayList;
import java.util.List;

import joist.codegen.Codegen;
import joist.codegen.dtos.CodeEntity;
import joist.codegen.dtos.CodeValue;
import joist.codegen.dtos.Entity;
import joist.codegen.dtos.ManyToOneProperty;
import joist.codegen.dtos.OneToManyProperty;
import joist.codegen.dtos.PrimitiveProperty;
import joist.domain.builders.AbstractBuilder;
import joist.sourcegen.Argument;
import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Inflector;
import joist.util.MapToList;

import org.apache.commons.lang.StringUtils;

public class GenerateBuilderCodegenPass implements Pass {

  public void pass(Codegen codegen) {
    for (Entity entity : codegen.getEntities().values()) {
      if (entity.isCodeEntity()) {
        continue;
      }

      GClass builderCodegen = codegen.getOutputCodegenDirectory().getClass(entity.getFullBuilderCodegenClassName());
      builderCodegen.setAbstract();
      if (entity.isRoot()) {
        builderCodegen.baseClassName("AbstractBuilder<{}>", entity.getFullClassName());
        builderCodegen.addImports(AbstractBuilder.class);
      } else {
        builderCodegen.baseClassName(entity.getParentClassName() + "Builder");
      }
      builderCodegen.addAnnotation("@SuppressWarnings(\"all\")");

      this.constructor(builderCodegen, entity);
      this.primitiveProperties(codegen, builderCodegen, entity);
      this.manyToOneProperties(builderCodegen, entity);
      this.oneToManyProperties(builderCodegen, entity);
      this.overrideGet(builderCodegen, entity);

      GMethod defaults = builderCodegen.getMethod("defaults");
      defaults.addAnnotation("@Override");
      defaults.returnType(entity.getBuilderClassName());
      defaults.body.line("return ({}) super.defaults();", entity.getBuilderClassName());
    }
  }

  private void constructor(GClass builderCodegen, Entity entity) {
    GMethod m = builderCodegen.getConstructor(entity.getFullClassName() + " instance");
    m.body.line("super(instance);");
  }

  private void overrideGet(GClass builderCodegen, Entity entity) {
    builderCodegen.getMethod("get").returnType(entity.getFullClassName()).body.line("return ({}) super.get();", entity.getFullClassName());
  }

  private void primitiveProperties(Codegen codegen, GClass c, Entity entity) {
    // first pass to get the types
    MapToList<String, String> perType = new MapToList<String, String>();
    for (PrimitiveProperty p : entity.getPrimitiveProperties()) {
      perType.get(p.getJavaType()).add(p.getVariableName());
    }
    // second pass to output the getters/setters
    for (PrimitiveProperty p : entity.getPrimitiveProperties()) {
      if (p.getVariableName().equals("version")) {
        continue;
      }
      // regular foo() getter
      this.addFluentGetter(c, p.getVariableName(), p.getJavaType());
      // regular foo(value) setter
      this.addFluentSetter(c, entity, p.getVariableName(), p.getJavaType());
      // overload with(value) setter
      if (perType.get(p.getJavaType()).size() == 1) {
        this.addFluentWith(c, entity, p.getVariableName(), p.getJavaType());
      }
      // add to defaults
      if (p.shouldHaveNotNullRule()) {
        String defaultValue;
        if (String.class.getName().equals(p.getJavaType())) {
          defaultValue = "\"" + p.getVariableName() + "\"";
        } else {
          defaultValue = codegen.getConfig().getBuildersDefault(p.getJavaType());
        }
        this.addToDefaults(c, p.getVariableName(), defaultValue);
      }
    }
  }

  private void oneToManyProperties(GClass c, Entity entity) {
    for (OneToManyProperty otom : entity.getOneToManyProperties()) {
      if (otom.isCollectionSkipped()) {
        continue;
      }
      if (otom.isOneToOne()) {
        // child() -> ChildBuilder
        GMethod m = c.getMethod(otom.getVariableNameSingular());
        m.returnType("{}Builder", otom.getTargetJavaType());
        m.body.line("if (get().get{}() == null) {", otom.getCapitalVariableNameSingular());
        m.body.line("_   return null;");
        m.body.line("}");
        m.body.line("return Builders.existing(get().get{}());", otom.getCapitalVariableNameSingular());
      } else {
        // childs() -> List<ChildBuilder>
        {
          GMethod m = c.getMethod(otom.getVariableName());
          m.returnType("List<{}Builder>", otom.getTargetJavaType());
          m.body.line("List<{}Builder> b = new ArrayList<{}Builder>();", otom.getTargetJavaType(), otom.getTargetJavaType());
          m.body.line("for ({} e : get().get{}()) {", otom.getTargetJavaType(), otom.getCapitalVariableName());
          m.body.line("_   b.add(Builders.existing(e));");
          m.body.line("}");
          m.body.line("return b;");
          c.addImports(List.class, ArrayList.class);
          c.addImports(otom.getManySide().getFullClassName());
        }
        // child(i) -> ChildBuilder
        {
          GMethod m = c.getMethod(StringUtils.uncapitalize(otom.getCapitalVariableNameSingular()), Argument.arg("int", "i"));
          m.returnType("{}Builder", otom.getTargetJavaType());
          m.body.line("return Builders.existing(get().get{}().get(i));", otom.getCapitalVariableName());
        }
      }
    }
  }

  private void manyToOneProperties(GClass c, Entity entity) {
    // first pass to get the types so we can add "with(Type)" if there is only 1 property of Type
    MapToList<String, String> perType = new MapToList<String, String>();
    // and also whether we have overlapping code names
    MapToList<String, String> perCodeName = new MapToList<String, String>();
    for (ManyToOneProperty mtop : entity.getManyToOneProperties()) {
      perType.get(mtop.getJavaType()).add(mtop.getVariableName());
      if (mtop.getOneSide().isCodeEntity()) {
        for (CodeValue code : ((CodeEntity) mtop.getOneSide()).getCodes()) {
          perCodeName.get(code.getEnumName()).add(mtop.getOneSide().getClassName());
        }
      }
    }
    // second pass to output the getters/setters
    for (ManyToOneProperty mtop : entity.getManyToOneProperties()) {
      // regular foo() getter
      if (mtop.getOneSide().isCodeEntity()) {
        this.addFluentGetter(c, mtop.getVariableName(), mtop.getJavaType());
      } else {
        this.addFluentBuilderGetter(c, mtop.getVariableName(), mtop.getJavaType());
      }

      // regular foo(value) setter
      this.addFluentSetter(c, entity, mtop.getVariableName(), mtop.getOneSide().getFullClassName());
      // overload with(value) setter
      if (perType.get(mtop.getJavaType()).size() == 1) {
        this.addFluentWith(c, entity, mtop.getVariableName(), mtop.getOneSide().getFullClassName());
      }

      if (!mtop.getOneSide().isCodeEntity()) {
        // regular foo(valueBuilder) setter
        this.addFluentBuilderSetter(c, entity, mtop.getVariableName(), mtop.getOneSide().getBuilderClassName());
        // overload with(valueBuilder) setter
        if (perType.get(mtop.getJavaType()).size() == 1) {
          this.addFluentWith(c, entity, mtop.getVariableName(), mtop.getOneSide().getBuilderClassName());
        }
      }

      // add to defaults
      if (mtop.isNotNull()) {
        if (mtop.getOneSide().isCodeEntity()) {
          CodeEntity ce = (CodeEntity) mtop.getOneSide();
          String defaultValue = ce.getClassName() + "." + ce.getCodes().get(0).getEnumName();
          this.addToDefaults(c, mtop.getVariableName(), defaultValue);
        } else if (!mtop.getOneSide().isAbstract()) {
          String defaultValue = "Builders.a" + mtop.getOneSide().getClassName() + "().defaults()";
          this.addToDefaults(c, mtop.getVariableName(), defaultValue);
        }
      }

      // add codeValue() methods
      if (mtop.getOneSide().isCodeEntity()) {
        for (CodeValue code : ((CodeEntity) mtop.getOneSide()).getCodes()) {
          if (perCodeName.get(code.getEnumName()).size() == 1) {
            {
              // e.g. blue()
              GMethod m = c.getMethod(Inflector.uncapitalize(code.getNameCamelCased()));
              m.returnType(entity.getBuilderClassName());
              m.body.line("return {}({}.{});", mtop.getVariableName(), mtop.getOneSide().getClassName(), code.getEnumName());
            }
            {
              // e.g. isBlue()
              GMethod m = c.getMethod("is" + code.getNameCamelCased()).returnType("boolean");
              m.body.line("return get().is{}();", code.getNameCamelCased());
            }
          }
        }
      }
    }
  }

  private void addFluentSetter(GClass builderCodegen, Entity entity, String variableName, String javaType) {
    GMethod m = builderCodegen.getMethod(variableName, Argument.arg(javaType, variableName));
    m.returnType(entity.getBuilderClassName());
    m.body.line("get().set{}({});", Inflector.capitalize(variableName), variableName);
    m.body.line("return ({}) this;", entity.getBuilderClassName());
  }

  private void addFluentWith(GClass builderCodegen, Entity entity, String variableName, String javaType) {
    GMethod m = builderCodegen.getMethod("with", Argument.arg(javaType, variableName));
    m.returnType(entity.getBuilderClassName());
    m.body.line("return {}({});", variableName, variableName);
  }

  private void addFluentBuilderSetter(GClass builderCodegen, Entity entity, String variableName, String javaType) {
    GMethod m = builderCodegen.getMethod(variableName, Argument.arg(javaType, variableName));
    m.returnType(entity.getBuilderClassName());
    m.body.line("return {}({}.get());", variableName, variableName);
  }

  private void addFluentGetter(GClass builderCodegen, String variableName, String javaType) {
    GMethod m = builderCodegen.getMethod(variableName);
    m.returnType(javaType);
    m.body.line("return get().get{}();", Inflector.capitalize(variableName));
  }

  private void addFluentBuilderGetter(GClass builderCodegen, String variableName, String javaType) {
    GMethod m = builderCodegen.getMethod(variableName);
    m.returnType(javaType + "Builder");
    m.body.line("if (get().get{}() == null) {", Inflector.capitalize(variableName));
    m.body.line("_   return null;");
    m.body.line("}");
    m.body.line("return Builders.existing(get().get{}());", Inflector.capitalize(variableName));
  }

  private void addToDefaults(GClass c, String variableName, String defaultValue) {
    GMethod defaults = c.getMethod("defaults");
    defaults.body.line("if ({}() == null) {", variableName);
    defaults.body.line("_   {}({});", variableName, defaultValue);
    defaults.body.line("}");
  }

}
