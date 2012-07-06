package features.domain.builders;

import features.domain.PrimitivesB;
import joist.domain.builders.AbstractBuilder;
import joist.domain.uow.UoW;

@SuppressWarnings("all")
public abstract class PrimitivesBBuilderCodegen extends AbstractBuilder<PrimitivesB> {

  public PrimitivesBBuilderCodegen(PrimitivesB instance) {
    super(instance);
  }

  public Long big1() {
    return get().getBig1();
  }

  public PrimitivesBBuilder big1(Long big1) {
    get().setBig1(big1);
    return (PrimitivesBBuilder) this;
  }

  public Long big2() {
    return get().getBig2();
  }

  public PrimitivesBBuilder big2(Long big2) {
    get().setBig2(big2);
    return (PrimitivesBBuilder) this;
  }

  @Override
  public PrimitivesBBuilder defaults() {
    if (big2() == null) {
      big2(0l);
    }
    if (bool2() == null) {
      bool2(false);
    }
    if (boolWithDefaultTrue() == null) {
      boolWithDefaultTrue(false);
    }
    if (int2() == null) {
      int2(0);
    }
    if (small2() == null) {
      small2((short) 0);
    }
    return (PrimitivesBBuilder) super.defaults();
  }

  public Boolean bool1() {
    return get().getBool1();
  }

  public PrimitivesBBuilder bool1(Boolean bool1) {
    get().setBool1(bool1);
    return (PrimitivesBBuilder) this;
  }

  public Boolean bool2() {
    return get().getBool2();
  }

  public PrimitivesBBuilder bool2(Boolean bool2) {
    get().setBool2(bool2);
    return (PrimitivesBBuilder) this;
  }

  public Boolean boolNullableWithDefaultFalse() {
    return get().getBoolNullableWithDefaultFalse();
  }

  public PrimitivesBBuilder boolNullableWithDefaultFalse(Boolean boolNullableWithDefaultFalse) {
    get().setBoolNullableWithDefaultFalse(boolNullableWithDefaultFalse);
    return (PrimitivesBBuilder) this;
  }

  public Boolean boolWithDefaultTrue() {
    return get().getBoolWithDefaultTrue();
  }

  public PrimitivesBBuilder boolWithDefaultTrue(Boolean boolWithDefaultTrue) {
    get().setBoolWithDefaultTrue(boolWithDefaultTrue);
    return (PrimitivesBBuilder) this;
  }

  public Long id() {
    if (UoW.isOpen() && get().getId() == null) {
      UoW.flush();
    }
    return get().getId();
  }

  public PrimitivesBBuilder id(Long id) {
    get().setId(id);
    return (PrimitivesBBuilder) this;
  }

  public Integer int1() {
    return get().getInt1();
  }

  public PrimitivesBBuilder int1(Integer int1) {
    get().setInt1(int1);
    return (PrimitivesBBuilder) this;
  }

  public Integer int2() {
    return get().getInt2();
  }

  public PrimitivesBBuilder int2(Integer int2) {
    get().setInt2(int2);
    return (PrimitivesBBuilder) this;
  }

  public Short small1() {
    return get().getSmall1();
  }

  public PrimitivesBBuilder small1(Short small1) {
    get().setSmall1(small1);
    return (PrimitivesBBuilder) this;
  }

  public Short small2() {
    return get().getSmall2();
  }

  public PrimitivesBBuilder small2(Short small2) {
    get().setSmall2(small2);
    return (PrimitivesBBuilder) this;
  }

  public PrimitivesB get() {
    return (features.domain.PrimitivesB) super.get();
  }

  @Override
  public PrimitivesBBuilder ensureSaved() {
    if (UoW.isOpen()) {
      if (get().getChanged().size() == 0) {
        throw new RuntimeException("instance has not been changed yet");
      }
      UoW.flush();
    } else {
      throw new RuntimeException("ensureSaved only works if the UoW is open");
    }
    return (PrimitivesBBuilder) this;
  }

}
