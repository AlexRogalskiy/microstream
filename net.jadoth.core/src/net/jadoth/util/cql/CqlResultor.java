package net.jadoth.util.cql;

import static net.jadoth.X.notNull;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jadoth.collections.sorting.Sortable;
import net.jadoth.collections.sorting.SortableProcedure;
import net.jadoth.collections.types.XIterable;
import net.jadoth.collections.types.XSequence;
import net.jadoth.functional.Aggregator;

public interface CqlResultor<O, R>
{
	public Aggregator<O, R> prepareCollector(XIterable<?> source);



	public static <O> CqlResultor<O, XSequence<O>> New()
	{
		return e -> new CqlWrapperCollectorProcedure<>(CQL.prepareTargetCollection(e));
	}

	public static <O, T extends Consumer<O> & XIterable<O>> CqlResultor<O, T> New(final T target)
	{
		notNull(target);
		return e -> new CqlWrapperCollectorProcedure<>(target);
	}

	// (06.07.2016 TM)NOTE: javac reports a false ambiguity here. Probably one of several bugs encountered when trying to use it.
	public static <O, R> CqlResultor<O, R> NewFromAggregator(final Aggregator<O, R> collector)
	{
		notNull(collector);
		return e -> collector;
	}

	// (06.07.2016 TM)NOTE: javac reports a false ambiguity here. Probably one of several bugs encountered when trying to use it.
	public static <O, T extends Consumer<O>> CqlResultor<O, T> NewFromSupplier(final Supplier<T> supplier)
	{
		return e -> new CqlWrapperCollectorProcedure<>(supplier.get());
	}

	public static <O, T extends SortableProcedure<O> & XIterable<O>> CqlResultor<O, T> NewFromSupplier(
		final Supplier<T>           supplier,
		final Comparator<? super O> order
	)
	{
		notNull(supplier);
		return order == null
			? CqlResultor.NewFromSupplier(supplier)
			: e -> new CqlWrapperCollectorSequenceSorting<>(supplier.get(), order)
		;
	}

	public static <O, T> CqlResultor<O, T> NewFromSupplier(final Supplier<T> supplier, final BiConsumer<O, T> linker)
	{
		final T target = supplier.get();
		return e -> new Aggregator<O, T>()
		{
			@Override
			public void accept(final O element)
			{
				linker.accept(element, target);
			}

			@Override
			public T yield()
			{
				return target;
			}
		};
	}

	public static <O, T extends Sortable<O>> CqlResultor<O, T> NewFromSupplier(
		final Supplier<T>           supplier,
		final BiConsumer<O, T>     linker  ,
		final Comparator<? super O> order
	)
	{
		notNull(supplier);
		return order == null
			? CqlResultor.NewFromSupplier(supplier, linker)
			: e -> new CqlWrapperCollectorLinkingSorting<>(supplier.get(), linker, order)
		;
	}

	public static <O, T> CqlResultor<O, T> NewFromSupplier(
		final Supplier<T>         supplier ,
		final BiConsumer<O, T>   linker   ,
		final Consumer<? super T> finalizer
	)
	{
		notNull(supplier);
		return finalizer == null
			? CqlResultor.NewFromSupplier(supplier, linker)
			: e -> new CqlWrapperCollectorLinkingFinalizing<>(supplier.get(), linker, finalizer)
		;
	}

}