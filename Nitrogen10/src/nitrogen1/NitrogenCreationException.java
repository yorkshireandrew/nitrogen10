package nitrogen1;

/** Exception that is thrown if the nitrogen package is unable to create a component */
public class NitrogenCreationException extends Exception{
	NitrogenCreationException(){super();}
	NitrogenCreationException(String s){super(s);}
}
