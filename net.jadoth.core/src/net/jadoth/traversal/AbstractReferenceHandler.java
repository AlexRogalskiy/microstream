package net.jadoth.traversal;

import net.jadoth.collections.types.XSet;

public abstract class AbstractReferenceHandler implements TraversalReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final TypeTraverserProvider  traverserProvider;
	final XSet<Object>           alreadyHandled   ;
	final TraversalPredicateSkip predicateSkip    ;
	final TraversalPredicateNode predicateNode    ;
	final TraversalPredicateLeaf predicateLeaf    ;
	final TraversalPredicateFull predicateFull    ;

	Object[] iterationTail      = ObjectGraphTraverser.Implementation.createIterationSegment();
	Object[] iterationHead      = this.iterationTail;
	boolean  tailIsHead         = true;
	int      iterationTailIndex;
	int      iterationHeadIndex;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AbstractReferenceHandler(
		final TypeTraverserProvider  traverserProvider,
		final XSet<Object>           alreadyHandled   ,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull
	)
	{
		super();
		this.traverserProvider = traverserProvider;
		this.alreadyHandled    = alreadyHandled   ;
		this.predicateSkip     = predicateSkip    ;
		this.predicateNode     = predicateNode    ;
		this.predicateLeaf     = predicateLeaf    ;
		this.predicateFull     = predicateFull    ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean skip(final Object instance)
	{
		return this.alreadyHandled.add(instance);
	}
	
	final void increaseIterationQueue()
	{
		final Object[] nextIterationSegment = ObjectGraphTraverser.Implementation.createIterationSegment();
		this.iterationHead[ObjectGraphTraverser.Implementation.SEGMENT_SIZE]    = nextIterationSegment    ;
		this.iterationHead      = nextIterationSegment;
		this.iterationHeadIndex = 0                   ;
		this.tailIsHead         = false               ;
	}
	
	@Override
	public final void enqueue(final Object instance)
	{
		// must check for null as there is no control over what custom handler implementations might pass.
		if(instance == null)
		{
			return;
		}
		if(!this.alreadyHandled.add(instance))
		{
			return;
		}
		if(this.predicateSkip != null && this.predicateSkip.skip(instance))
		{
			return;
		}
						
		if(this.iterationHeadIndex >= ObjectGraphTraverser.Implementation.SEGMENT_SIZE)
		{
			this.increaseIterationQueue();
		}
		this.iterationHead[this.iterationHeadIndex++] = instance;
	}
				
	private Object dequeue()
	{
		if(this.tailIsHead)
		{
			this.checkForCompletion();
		}
		if(this.iterationTailIndex >= ObjectGraphTraverser.Implementation.SEGMENT_SIZE)
		{
			this.advanceSegment();
		}
		
		return this.iterationTail[this.iterationTailIndex++];
	}
		
	final void checkForCompletion()
	{
		if(this.iterationTailIndex >= this.iterationHeadIndex)
		{
			ObjectGraphTraverser.signalAbortTraversal();
		}
	}
	
	final void advanceSegment()
	{
		this.iterationTail      = (Object[])this.iterationTail[ObjectGraphTraverser.Implementation.SEGMENT_SIZE];
		this.iterationTailIndex = 0;
		this.tailIsHead         = this.iterationTail == this.iterationHead;
	}

	private void enqueueAll(final Object[] instances)
	{
		for(final Object instance : instances)
		{
			this.enqueue(instance);
		}
	}
	
	@Override
	public final void handleAsFull(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object                instance  = this.dequeue();
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateLeaf != null && this.predicateLeaf.isLeaf(instance))
				{
					this.handleLeaf(instance, traverser);
				}
				else if(this.predicateNode != null && this.predicateNode.isNode(instance))
				{
					this.handleNode(instance, traverser);
				}
				else
				{
					this.handleFull(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	@Override
	public final void handleAsNode(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object                instance  = this.dequeue();
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateFull != null && this.predicateFull.isFull(instance))
				{
					this.handleFull(instance, traverser);
				}
				else if(this.predicateLeaf != null && this.predicateLeaf.isLeaf(instance))
				{
					this.handleLeaf(instance, traverser);
				}
				else
				{
					this.handleNode(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	@Override
	public final void handleAsLeaf(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object                instance  = this.dequeue();
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateFull != null && this.predicateFull.isFull(instance))
				{
					this.handleFull(instance, traverser);
				}
				else if(this.predicateNode != null && this.predicateNode.isNode(instance))
				{
					this.handleNode(instance, traverser);
				}
				else
				{
					this.handleLeaf(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	

	abstract <T> void handleFull(T instance, final TypeTraverser<T> traverser);
	
	abstract <T> void handleLeaf(T instance, final TypeTraverser<T> traverser);
	
	final <T> void handleNode(final T instance, final TypeTraverser<T> traverser)
	{
//		JadothConsole.debugln("Traversing NODE " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(traverser));
		traverser.traverseReferences(instance, this);
	}
	
}
