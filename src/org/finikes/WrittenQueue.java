package org.finikes;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class WrittenQueue<E> implements Queue<E> {
	public boolean isEmpty() {
		return first == null;
	}

	private QueueElement first;
	private QueueElement last;

	public boolean offer(E o) {
		QueueElement qe = new QueueElement(o);
		if (isEmpty()) {
			first = qe;
			return true;
		}

		if (last == null) {
			first.next = qe;
			last = qe;
			return true;
		}

		last.next = qe;
		last = qe;
		return true;
	}

	public E poll() {
		if (first == null) {
			return null;
		}

		E result = first.element;
		first = first.next;
		return result;
	}

	public E peek() {
		if (first == null) {
			return null;
		}

		return first.element;
	}

	private class QueueElement {
		E element;
		QueueElement next;

		private QueueElement(E element) {
			this.element = element;
		}
	}

	public E remove() {
		if (first != null) {
			QueueElement e = first;
			first = first.next;
			return e.element;
		}

		return null;
	}

	@Override
	public int size() {
		throw new RuntimeException("No support method.");
	}

	@Override
	public boolean contains(Object o) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public Iterator<E> iterator() {
		throw new RuntimeException("No support method.");
	}

	@Override
	public Object[] toArray() {
		throw new RuntimeException("No support method.");
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public void clear() {
		throw new RuntimeException("No support method.");
	}

	@Override
	public boolean add(E e) {
		throw new RuntimeException("No support method.");
	}

	@Override
	public E element() {
		throw new RuntimeException("No support method.");
	}
}
