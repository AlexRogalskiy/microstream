package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_int extends AbstractBinaryHandlerNativeArrayPrimitive<int[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_int()
	{
		super(int[].class, defineElementsType(int.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final int[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_int(this.typeId(), oid, array);
	}

	@Override
	public int[] create(final Binary bytes)
	{
		return bytes.createArray_int();
	}

	@Override
	public void update(final Binary bytes, final int[] instance, final PersistenceLoadHandler builder)
	{
		bytes.updateArray_int(instance);
	}

}