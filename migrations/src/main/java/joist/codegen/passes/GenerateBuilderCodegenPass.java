package joist.codegen.passes;

import java.util.ArrayList;
import java.util.List;

import joist.codegen.Codegen;
import joist.codegen.dtos.CodeEntity;
import joist.codegen.dtos.CodeValue;
import joist.codegen.dtos.Entity;
import joist.codegen.dtos.ManyToManyProperty;
import joist.codegen.dtos.ManyToOneProperty;
import joist.codegen.dtos.OneToManyProperty;
import joist.codegen.dtos.PrimitiveProperty;
import joist.domain.builders.AbstractBuilder;
import joist.domain.uow.UoW;
import joist.sourcegen.Argument;
import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;
import joist.util.Inflector;
import joist.util.MapToList;

import org.apache.commons.lang.StringUtils;

public class GenerateBuilderCodegenPass implements Pass<Codegen> {

  public void pass(Codegen codegen) {
    for (Entity entity : codegen.getSchema().getEntities().values()) {
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
      this.manyToManyProperties(builderCodegen, entity);
      this.overrideGet(builderCodegen, entity);
      this.ensureSaved(builderCodegen, entity);

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

  private void ensureSaved(GClass builderCodegen, Entity entity) {
    GMethod m = builderCodegen.getMethod("ensureSaved").returnType(entity.getBuilderClassName()).addAnnotation("@Override");
    m.body.line("doEnsureSaved();");
    m.body.line("return ({}) this;", entity.getBuilderClassName());
    builderCodegen.addImports(UoW.class);
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
      if (p.getVariableName().equals("id")) {
        GMethod m = c.getMethod("id").returnType(p.getJavaType());
        m.body.line("if (UoW.isOpen() && get().getId() == null) {");
        m.body.line("_   UoW.flush();");
        m.body.line("}");
        // let addFluentGetter call below finish the method (add the return)
        c.addImports(UoW.class);
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
        // user types may not have configured defaults
        if (defaultValue != null) {
          this.addToDefaults(c, p.getVariableName(), defaultValue);
        }
      }
    }
  }

  private void oneToManyProperties(GClass c, Entity entity) {
    for (OneToManyProperty otom : entity.getOneToManyProperties()) {
      if (otom.isCollectionSkipped() || otom.isManyToMany()) {
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

  private void manyToManyProperties(GClass c, Entity entity) {
    for (ManyToManyProperty mtmp : entity.getManyToManyProperties()) {
      if (mtmp.getMySideOneToMany().isCollectionSkipped() || mtmp.getTargetTable().isCodeEntity()) {
        continue;
      }

      // childs() -> List<ChildBuilder>
      {
        GMethod m = c.getMethod(mtmp.getVariableName());
        m.returnType("List<{}Builder>", mtmp.getTargetJavaType());
        m.body.line("List<{}Builder> b = new ArrayList<{}Builder>();", mtmp.getTargetJavaType(), mtmp.getTargetJavaType());
        m.body.line("for ({} e : get().get{}()) {", mtmp.getTargetJavaType(), mtmp.getCapitalVariableName());
        m.body.line("_   b.add(Builders.existing(e));");
        m.body.line("}");
        m.body.line("return b;");
        c.addImports(List.class, ArrayList.class);
        c.addImports(mtmp.getTargetTable().getFullClassName());
      }
      // child(i) -> ChildBuilder
      {
        GMethod m = c.getMethod(StringUtils.uncapitalize(mtmp.getCapitalVariableNameSingular()), Argument.arg("int", "i"));
        m.returnType("{}Builder", mtmp.getTargetJavaType());
        m.body.line("return Builders.existing(get().get{}().get(i));", mtmp.getCapitalVariableName());
      }

      this.addFluentSetter(c, entity, mtmp, false, mtmp.getCapitalVariableNameSingular()); // foo(OtherType)
      this.addFluentSetter(c, entity, mtmp, true, mtmp.getCapitalVariableNameSingular()); // foo(OtherTypeBuilder)
      this.addFluentSetter(c, entity, mtmp, false, "with"); // with(OtherType)
      this.addFluentSetter(c, entity, mtmp, true, "with"); // with(OtherTypeBuilder)
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

  private void addFluentSetter(GClass builderCodegen, Entity entity, ManyToManyProperty mtmp, boolean isForBuilder, String methodName) {
    final String arg;
    if (isForBuilder) {
      arg = mtmp.getTargetTable().getBuilderClassName();
    } else {
      arg = mtmp.getTargetTable().getFullClassName();
    }
    GMethod m = builderCodegen.getMethod(Inflector.uncapitalize(methodName), Argument.arg(arg, mtmp.getVariableName()));
    m.returnType(entity.getBuilderClassName());
    if (isForBuilder) {
      m.body.line("get().add{}({}.get());", mtmp.getCapitalVariableNameSingular(), mtmp.getVariableName());
    } else {
      m.body.line("get().add{}({});", mtmp.getCapitalVariableNameSingular(), mtmp.getVariableName());
    }
    m.body.line("return ({}) this;", entity.getBuilderClassName());
  }

  private void addToDefaults(GClass c, String variableName, String defaultValue) {
    GMethod defaults = c.getMethod("defaults");
    defaults.body.line("if ({}() == null) {", variableName);
    defaults.body.line("_   {}({});", variableName, defaultValue);
    defaults.body.line("}");
  }

}
