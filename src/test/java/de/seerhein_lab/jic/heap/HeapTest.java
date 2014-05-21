package de.seerhein_lab.jic.heap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.seerhein_lab.jic.vm.Array;
import de.seerhein_lab.jic.vm.ClassInstance;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.ReferenceSlot;
import de.seerhein_lab.jic.vm.UnknownObject;

public class HeapTest {
	private Heap heap;
	private ClassInstance a;
	private ClassInstance b;
	private ClassInstance c;
	private ClassInstance d;
	private ClassInstance e;
	private Array f;

	private ReferenceSlot aRef;
	private ReferenceSlot bRef;
	private ReferenceSlot cRef;
	private ReferenceSlot dRef;
	private ReferenceSlot eRef;
	private ReferenceSlot fRef;

	public void setUpHeap() {
		heap = new Heap();

		a = heap.newClassInstance(false, "");
		b = heap.newClassInstance(false, "");
		c = heap.newClassInstance(false, "");
		d = heap.newClassInstance(false, "");
		e = heap.newClassInstance(false, "");
		f = heap.newArray("");

		aRef = new ReferenceSlot(a);
		bRef = new ReferenceSlot(b);
		cRef = new ReferenceSlot(c);
		dRef = new ReferenceSlot(d);
		eRef = new ReferenceSlot(e);
		fRef = new ReferenceSlot(f);
	}

	@Before
	public void setUpReferences() {
		setUpHeap();

		a.setField("f", b);
		b.setField("f", c);
		b.setField("f2", d);
		c.setField("f", e);
		d.setField("f", e);
		e.setField("f", f);
		f.addReferredObject(d);
	}

	@Test
	public void testInitalization() {
		// assertNotNull(heap.getMutableExternalObject());
		assertNotNull(heap.getThisInstance());
	}

	@Test
	public void testPublishNull() {
		heap.publish(null);
	}

	@Test
	public void testPublish() {
		assertEquals(a, aRef.getObject(heap));
		assertEquals(b, bRef.getObject(heap));
		assertEquals(c, cRef.getObject(heap));
		assertEquals(d, dRef.getObject(heap));
		assertEquals(e, eRef.getObject(heap));
		assertEquals(f, fRef.getObject(heap));

		heap.publish(b);

		assertEquals(a, aRef.getObject(heap));
		assertTrue(bRef.getObject(heap) instanceof UnknownObject);
		assertTrue(cRef.getObject(heap) instanceof UnknownObject);
		assertTrue(dRef.getObject(heap) instanceof UnknownObject);
		assertTrue(eRef.getObject(heap) instanceof UnknownObject);
		assertTrue(fRef.getObject(heap) instanceof UnknownObject);
	}

	@Test
	public void testNotPublishThis() {
		UnknownObject thisInstance = heap.getThisInstance();
		heap.publish(thisInstance);
		// assertEquals(thisInstance, heap.get(thisInstance.getId()));
		assertEquals(thisInstance, heap.getThisInstance());
	}

	@Test
	public void testRepublish() {
		assertEquals(f, fRef.getObject(heap));
		heap.publish(f);
		assertTrue(fRef.getObject(heap) instanceof UnknownObject);
		heap.publish(f);
		assertTrue(fRef.getObject(heap) instanceof UnknownObject);
	}
}
