package laba1;

/**
 * My custom exception class.
 */

class ForbiddenException extends Exception
{
  public ForbiddenException(String message)
  {
    super(message);
  }
}