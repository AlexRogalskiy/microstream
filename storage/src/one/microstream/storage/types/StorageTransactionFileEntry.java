package one.microstream.storage.types;


/**
 * The representation of a data file inside a transactions file, accumulated from multiple transactions entries.
 *
 * @author Thomas Muenz
 */
public interface StorageTransactionFileEntry
{
	public long fileNumber();

	public long length();

	public boolean isDeleted();



	public final class Default implements StorageTransactionFileEntry
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long fileNumber;
		final long length    ;

		boolean isDeleted;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final long fileNumber, final long length)
		{
			super();
			this.fileNumber = fileNumber;
			this.length     = length    ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long fileNumber()
		{
			return this.fileNumber;
		}

		@Override
		public final long length()
		{
			return this.length;
		}

		@Override
		public final boolean isDeleted()
		{
			return this.isDeleted;
		}

	}

}
