/**
 * Created by xiaohe on 1/31/15.
 */
public class CompareIntParsing {
    public static void main(String[] args) {
        CompareIntParsing cip = new CompareIntParsing();
        String bigNum = "201399929";
        System.out.println(cip.parseIntByDigits(bigNum.getBytes()));

        System.out.println(cip.parseIntByStdLib(bigNum.getBytes()));
    }

    public int parseIntByDigits(byte[] number) {
        int result = 0;

        for (int i = 0; i < number.length; i++) {

            byte b = number[i];
            if (b > 47 && b < 58) {
                result *= 10;

                switch (b) {
                    case 48: //0
                        break;

                    case 49: //1
                        result += 1;
                        break;

                    case 50: //2
                        result += 2;
                        break;

                    case 51: //3
                        result += 3;
                        break;

                    case 52: //4
                        result += 4;
                        break;

                    case 53: //5
                        result += 5;
                        break;

                    case 54: //6
                        result += 6;
                        break;

                    case 55: //7
                        result += 7;
                        break;

                    case 56: //8
                        result += 8;
                        break;

                    case 57: //9
                        result += 9;
                        break;


                }
            }
        }

        return result;
    }

    public int parseIntByStdLib(byte[] number) {
        return Integer.parseInt(new String(number));
    }
}
