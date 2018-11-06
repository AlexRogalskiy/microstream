package net.jadoth.exceptions;


/**
 * Base class for all exceptions that workarounds some design mistakes in JDK exceptions.
 * For example disposing of the impractical, dangerous and clean-code-preventing checked exceptions or a proper
 * distinction between assembling the output string and querying the custom message.
 * 
 * @author TM
 */
public class BaseException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BaseException()
	{
		super();
	}

	public BaseException(final Throwable cause)
	{
		super(cause);
	}

	public BaseException(final String message)
	{
		super(message);
	}

	public BaseException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public BaseException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final String message()
	{
		return super.getMessage();
	}

	public String assembleDetailString()
	{
		return null;
	}

	protected String assembleExplicitMessageAddon()
	{
		final String explicitMessage = this.message();
		return explicitMessage != null
			? " (" + explicitMessage + ")"
			: ""
		;
	}

	public String assembleOutputString()
	{
		// JDK concept or improved concept based on if assembleDetailString is overwritten
		final String detailString = this.assembleDetailString();
		return detailString == null
			? super.getMessage()
			: this.assembleDetailString() + this.assembleExplicitMessageAddon()
		;
	}

	/**
	 * Due to bad class design in the JDK's {@link Throwable}, this getter-named methods actually serves as
	 * the output string assembly method.<br>
	 * For the actual message getter, see {@link #message()} (which is a preferable name, anyway).<br>
	 * For the actually executed logic, see {@link #assembleOutputString()}, which is called by this method.<br>
	 *
	 * @return this exception type's generic output string plus an explicit message if present.
	 */
	@Override
	public String getMessage()
	{
		return this.assembleOutputString();
	}
	
}