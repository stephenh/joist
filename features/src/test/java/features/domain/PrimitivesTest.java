package features.domain;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PrimitivesTest extends AbstractFeaturesTest {

  @Test
  public void testSave() {
    Primitives foo = new Primitives("testSave");
    this.commitAndReOpen();
    Assert.assertEquals(1, foo.getId().intValue());

    Primitives reloaded = Primitives.queries.find(1);
    Assert.assertEquals(1, reloaded.getId().intValue());
    Assert.assertEquals("testSave", reloaded.getName());
  }

  @Test
  public void testFlushMeansWeCanFindItRightAway() {
    new Primitives("testSave");
    this.flush();

    Primitives reloaded = Primitives.queries.find(1);
    Assert.assertEquals("testSave", reloaded.getName());
  }

  @Test
  public void testRollbackAfterFlushMeansItIsNotThere() {
    new Primitives("testSave");
    this.flush();
    this.rollback();

    try {
      Primitives.queries.findByName("testSave");
      Assert.fail();
    } catch (Exception e) {
      Assert.assertEquals("Instance of Primitives not found", e.getMessage());
    }
  }

  @Test
  public void testSaveTwoInSameUowGetDifferentIds() {
    new Primitives("foo");
    new Primitives("bar");
    this.commitAndReOpen();
    Assert.assertEquals(1, Primitives.queries.findByName("foo").getId().intValue());
    Assert.assertEquals(2, Primitives.queries.findByName("bar").getId().intValue());
  }

  @Test
  public void testLoadViaIdTwiceReturnsTheSameInstance() {
    new Primitives("foo");
    this.commitAndReOpen();
    Primitives twp1 = Primitives.queries.find(1);
    Primitives twp2 = Primitives.queries.find(1);
    Assert.assertTrue(twp1 == twp2);
  }

  @Test
  public void testLoadViaIdThenNameReturnsTheSameInstance() {
    new Primitives("foo");
    this.commitAndReOpen();
    Primitives twp1 = Primitives.queries.find(1);
    Primitives twp2 = Primitives.queries.findByName("foo");
    Assert.assertTrue(twp1 == twp2);
  }

  @Test
  public void testUpdateTicksVersion() {
    new Primitives("foo");
    this.commitAndReOpen();

    Primitives twp = Primitives.queries.find(1);
    Assert.assertEquals(0, twp.getVersion().intValue());
    twp.setName("bar");
    this.commitAndReOpen();
    // Should already see it tick
    Assert.assertEquals(1, twp.getVersion().intValue());

    // And after reloading
    twp = this.reload(twp);
    Assert.assertEquals(1, twp.getVersion().intValue());
  }

  @Test
  public void testFindAllIds() {
    new Primitives("foo1");
    new Primitives("foo2");
    this.commitAndReOpen();
    List<Long> ids = Primitives.queries.findAllIds();
    Assert.assertEquals(2, ids.size());
    Assert.assertEquals(1, ids.get(0).intValue());
    Assert.assertEquals(2, ids.get(1).intValue());
  }

  @Test
  public void testGeneratedFindByName() {
    new Primitives("foo1");
    this.commitAndReOpen();
    Primitives p = Primitives.queries.findByName("foo1");
    Assert.assertEquals("foo1", p.getName());
  }

  @Test
  public void testWithIdAndWithoutId() {
    new Primitives("a");
    Primitives b = new Primitives("b");
    b.setId(10l);
    this.commitAndReOpen();
    Assert.assertEquals(2l, Primitives.queries.count());
  }

  @Test
  public void testFindIds() {
    new Primitives("foo1");
    new Primitives("foo2");
    this.commitAndReOpen();
    Primitives p1 = Primitives.queries.find(1);
    Primitives p2 = Primitives.queries.find(1);
    List<Primitives> objs = Primitives.queries.find(p1.getId(), p2.getId());
    Assert.assertEquals(2, objs.size());
    Assert.assertTrue(objs.contains(p1));
    Assert.assertTrue(objs.contains(p2));
  }

  @Test
  public void testFindInBatches() {
    new Primitives("foo1");
    new Primitives("foo2");
    this.commitAndReOpen();
    Integer batchSize = 1;
    Iterator<List<Primitives>> batches = Primitives.queries.findAllInBatches(batchSize);
    Assert.assertTrue(batches.hasNext());
    List<Primitives> batch1 = batches.next();
    Assert.assertTrue(batch1.size() <= batchSize);
    Assert.assertTrue(batches.hasNext());
    List<Primitives> batch2 = batches.next();
    Assert.assertTrue(batch2.size() <= batchSize);
    Assert.assertFalse(batches.hasNext());
  }

  @Test
  public void testFindAllInBatchesWithRemainder() {
    int totalPrimitives = 5;
    for (int i = 0; i < totalPrimitives; i++) {
      new Primitives("foo" + String.valueOf(i));
    }
    this.commitAndReOpen();
    Integer batchSize = 3;
    Iterator<List<Primitives>> batches = Primitives.queries.findAllInBatches(batchSize);
    Assert.assertTrue(batches.hasNext());
    List<Primitives> batch1 = batches.next();
    Assert.assertEquals(batch1.size(), 3);
    List<Primitives> batch2 = batches.next();
    Assert.assertEquals(batch2.size(), 2);
    Assert.assertFalse(batches.hasNext());
  }

}
