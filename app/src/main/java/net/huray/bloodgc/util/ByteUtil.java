package net.huray.bloodgc.util;


public class ByteUtil {

	public static String ConvertHexByteToString(byte byteToConvert)
	{
		String ConvertedByte = "";
		if(byteToConvert < 0)
		{
			ConvertedByte += Integer.toString(byteToConvert + 256, 16) + " ";
		}
		else if(byteToConvert <= 15)
		{
			ConvertedByte += "0" + Integer.toString(byteToConvert, 16) + " ";
		}
		else
		{
			ConvertedByte += Integer.toString(byteToConvert, 16) + " ";
		}

		return ConvertedByte;
	}
	
	public static String ConvertHexByteArrayToString(byte[] byteArrayToConvert)
	{
		StringBuffer convertByte = new StringBuffer("");
		for (int i = 0; i < byteArrayToConvert.length; i++)
		{
			convertByte.append(ByteUtil.ConvertHexByteToString(byteArrayToConvert[i]));
		}

		return convertByte.toString();
	}
	
	// ***********************************************************************/
	// * the function Convert a "2 bytes Array" To int Format
	// * (decimal)1876 = (hexadecimal)0754
	// * Example : Convert2bytesHexaFormatToInt {0x07, 0x54} -> returns 1876
	// ***********************************************************************/
	public static int Convert2bytesHexaFormatToInt(byte[] ArrayToConvert)
	{
		int ConvertedNumber = 0;
		if(ArrayToConvert[1] <= -1)// <0
			ConvertedNumber += ArrayToConvert[1] + 256;
		else
			ConvertedNumber += ArrayToConvert[1];

		if(ArrayToConvert[0] <= -1)// <0
			ConvertedNumber += (ArrayToConvert[0] * 256) + 256;
		else
			ConvertedNumber += ArrayToConvert[0] * 256;

		return ConvertedNumber;
	}
	
	// ***********************************************************************/
	// * the function Convert Int value to a "2 bytes Array" Format
	// * (decimal)1876 == (hexadecimal)0754
	// * Example : ConvertIntTo2bytesHexaFormat (1876) -> returns {0x07, 0x54}
	// ***********************************************************************/
	public static byte[] ConvertIntTo2bytesHexaFormat(int numberToConvert)
	{
		byte[] ConvertedNumber = new byte[2];

		ConvertedNumber[0] = (byte) (numberToConvert / 256);
		ConvertedNumber[1] = (byte) (numberToConvert - (256 * (numberToConvert / 256)));

		return ConvertedNumber;
	}
	
	// ***********************************************************************/
	// * the function Convert String to an Int value
	// ***********************************************************************/
	public static int ConvertStringToInt(String nbOfBlocks)
	{
		int count = 0;

		if(nbOfBlocks.length() > 2)
		{
			String msb = nbOfBlocks.substring(0, 2);
			String lsb = nbOfBlocks.substring(2, 4);

			count = Integer.parseInt(lsb, 16);
			count += (Integer.parseInt(msb, 16)) * 256;
		}
		else
		{
			String lsb = nbOfBlocks.substring(0, 2);
			count = Integer.parseInt(lsb, 16);
		}

		return count;
	}
	
	// ***********************************************************************/
	// * the function Convert a "4-char String" to a two bytes format
	// * Example : "0F43" -> { 0X0F ; 0X43 }
	// ***********************************************************************/
	public static byte[] ConvertStringToHexBytes(String StringToConvert)
	{
		StringToConvert = StringToConvert.toUpperCase();
		StringToConvert = StringToConvert.replaceAll(" ", "");
		char[] CharArray = StringToConvert.toCharArray();
		char[] Char = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int result = 0;
		byte[] ConvertedString = new byte[] { (byte) 0x00, (byte) 0x00 };
		for (int i = 0; i <= 1; i++)
		{

			for (int j = 0; j <= 15; j++)
			{
				if(CharArray[i] == Char[j])
				{
					if(i == 1)
					{
						result = result + j;
						j = 15;
					}

					else if(i == 0)
					{
						result = result + j * 16;
						j = 15;
					}

				}
			}
		}
		ConvertedString[0] = (byte) result;

		result = 0;
		for (int i = 2; i <= 3; i++)
		{
			for (int j = 0; j <= 15; j++)
			{
				if(CharArray[i] == Char[j])
				{
					if(i == 3)
					{
						result = result + j;
						j = 15;
					}

					else if(i == 2)
					{
						result = result + j * 16;
						j = 15;
					}

				}
			}
		}
		ConvertedString[1] = (byte) result;

		return ConvertedString;
	}
	
	// ***********************************************************************/
	// * the function Convert a "4-char String" to a two bytes format
	// * Example : "43" -> { 0X43 }
	// ***********************************************************************/
	public static byte ConvertStringToHexByte(String StringToConvert)
	{
		StringToConvert = StringToConvert.toUpperCase();
		char[] CharArray = StringToConvert.toCharArray();
		char[] Char = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int result = 0;
		for (int i = 0; i <= 1; i++)
		{
			for (int j = 0; j <= 15; j++)
			{
				if(CharArray[i] == Char[j])
				{
					if(i == 1)
					{
						result = result + j;
						j = 15;
					}

					else if(i == 0)
					{
						result = result + j * 16;
						j = 15;
					}
				}
			}
		}
		return (byte) result;
	}
	
	// ***********************************************************************/
	// * the function Convert a "4-char String" to a X bytes format
	// * Example : "0F43" -> { 0X0F ; 0X43 }
	// ***********************************************************************/
	public static byte[] ConvertStringToHexBytesArray(String StringToConvert)
	{
		StringToConvert = StringToConvert.toUpperCase();
		StringToConvert = StringToConvert.replaceAll(" ", "");
		char[] CharArray = StringToConvert.toCharArray();
		char[] Char = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int result = 0;
		byte[] ConvertedString = new byte[StringToConvert.length() / 2];
		int iStringLen = (StringToConvert.length());

		for (int i = 0; i < iStringLen; i++)
		{
			for (int j = 0; j <= 15; j++)
			{
				if(CharArray[i] == Char[j])
				{
					if(i % 2 == 1)
					{
						result = result + j;
						j = 15;
					}

					else if(i % 2 == 0)
					{
						result = result + j * 16;
						j = 15;
					}

				}
			}
			if(i % 2 == 1)
			{
				ConvertedString[i / 2] = (byte) result;
				result = 0;
			}
		}

		return ConvertedString;
	}
	
	// ***********************************************************************/
	// * the function cast a String to hexa character only
	// * when a character is not hexa it's replaced by '0'
	// * Example : ConvertHexByteToString ("AZER") -> returns "AFEF"
	// * Example : ConvertHexByteToString ("12l./<4") -> returns "12FFFF4"
	// ***********************************************************************/
	public static String castHexKeyboard(String sInput)
	{
		String sOutput = "";

		sInput = sInput.toUpperCase();
		char[] cInput = sInput.toCharArray();

		for (int i = 0; i < sInput.length(); i++)
		{
			if(cInput[i] != '0' && cInput[i] != '1' && cInput[i] != '2' && cInput[i] != '3' && cInput[i] != '4' && cInput[i] != '5' && cInput[i] != '6' && cInput[i] != '7' && cInput[i] != '8' && cInput[i] != '9' && cInput[i] != 'A' && cInput[i] != 'B' && cInput[i] != 'C' && cInput[i] != 'D'
					&& cInput[i] != 'E')
			{
				cInput[i] = 'F';
			}
			sOutput += cInput[i];
		}

		return sOutput;
	}
	
	// ***********************************************************************/
	// * the function verify and convert the start address from the EditText
	// * in order to not read out of memory range and code String on 4chars.
	// * Example : FormatStringAddressStart ("0F") -> returns "000F"
	// * Example : FormatStringAddressStart ("FFFF") -> returns "07FF"
	// ***********************************************************************/
	public static String FormatStringAddressStart(String stringToFormat, String memorySize)
	{
		String stringFormated = stringToFormat;
		stringFormated = StringForceDigit(stringToFormat, 4);

		if(stringFormated.length() > 4)
		{
			stringFormated = memorySize.replace(" ", "");
		}

		int iAddressStart = ConvertStringToInt(stringFormated);
		int iAddresStartMax = ConvertStringToInt(StringForceDigit(memorySize, 4));

		if(iAddressStart > iAddresStartMax)
		{
			iAddressStart = iAddresStartMax;
		}

		stringFormated = ByteUtil.ConvertIntToHexFormatString(iAddressStart);

		return stringFormated.toUpperCase();
	}
	
	// ***********************************************************************/
	// * the function Format a String with the right number of digit
	// * Example : ConvertHexByteToString ("23",4) -> returns "0023"
	// * Example : ConvertHexByteToString ("54",7) -> returns "0000054"
	// ***********************************************************************/
	public static String StringForceDigit(String sStringToFormat, int nbOfDigit)
	{
		String sStringFormated = sStringToFormat.replaceAll(" ", "");

		if(sStringFormated.length() == 4)
		{
			return sStringFormated;
		}
		else if(sStringFormated.length() < nbOfDigit)
		{
			while (sStringFormated.length() != nbOfDigit)
			{
				sStringFormated = "0".concat(sStringFormated);
			}
		}

		return sStringFormated;
	}

	// ***********************************************************************/
	// * the function convert an Int value to a String with Hexadecimal format
	// * Example : ConvertIntToHexFormatString (2047) -> returns "7FF"
	// ***********************************************************************/
	public static String ConvertIntToHexFormatString(int iNumberToConvert)
	{
		String sConvertedNumber = "";
		byte[] bNumberToConvert;

		bNumberToConvert = ConvertIntTo2bytesHexaFormat(iNumberToConvert);
		sConvertedNumber = ConvertHexByteArrayToString(bNumberToConvert);
		sConvertedNumber = sConvertedNumber.replaceAll(" ", "");
		return sConvertedNumber;
	}
	
   public static String convertHexToString(String hex){
    	 
	   StringBuilder sb = new StringBuilder();
	   StringBuilder temp = new StringBuilder();
	 
	   //49204c6f7665204a617661 split into two characters 49, 20, 4c...
	   for( int i=0; i<hex.length()-1; i+=2 ){
	 
	       //grab the hex in pairs
	       String output = hex.substring(i, (i + 2));
	       //convert hex to decimal
	       int decimal = Integer.parseInt(output, 16);
	       //convert the decimal to character
	       sb.append((char)decimal);
	 
	       temp.append(decimal);
	   }
	   System.out.println("Decimal : " + temp.toString());
	 
	   return sb.toString();
	  }
}
