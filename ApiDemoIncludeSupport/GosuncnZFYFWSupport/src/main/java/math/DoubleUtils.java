package java.math;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 由于Java的简单类型不能够精确的对浮点数进行运算，这个工具类提供精 确的浮点数运算，包括加减乘除和四舍五入。
 */
public class DoubleUtils{
    private static final int DEF_DIV_SCALE = 2;

    /**
     * @Description 两个Double数相加
     *
     * @param d1
     * @param d2
     * @return Double
     */
    public static Double add(Double d1,Double d2){
        BigDecimal b1 = new BigDecimal(d1.toString());
        BigDecimal b2 = new BigDecimal(d2.toString());
        return b1.add(b2).doubleValue();
    }

    /**
     * @Description 两个Double数相减
     *
     * @param d1
     * @param d2
     * @return Double
     */
    public static Double sub(Double d1,Double d2){
        BigDecimal b1 = new BigDecimal(d1.toString());
        BigDecimal b2 = new BigDecimal(d2.toString());
        return b1.subtract(b2).doubleValue();
    }

    /**
     * @Description 两个Double数相乘
     *
     * @param d1
     * @param d2
     * @return Double
     */
    public static Double mul(Double d1,Double d2){
        BigDecimal b1 = new BigDecimal(d1.toString());
        BigDecimal b2 = new BigDecimal(d2.toString());
        return b1.multiply(b2).doubleValue();
    }

    /**
     * @Description 两个Double数相除
     *
     * @param d1
     * @param d2
     * @return Double
     */
    public static Double div(Double d1,Double d2){
        BigDecimal b1 = new BigDecimal(d1.toString());
        BigDecimal b2 = new BigDecimal(d2.toString());
        return b1.divide(b2,DEF_DIV_SCALE,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * @Description 两个Double数相除，并保留scale位小数
     *
     * @param d1
     * @param d2
     * @param scale
     * @return Double
     */
    public static Double div(Double d1,Double d2,int scale){
        if(scale<0){
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(d1.toString());
        BigDecimal b2 = new BigDecimal(d2.toString());
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * @Description String类型小数与Double类型的转换
     */
    public static void StrToDouble(){
        String str="1234.5678";
        double num;
        DecimalFormat myformat = new DecimalFormat("#0.00");
        num = Double.parseDouble(str);//直接转换为double类型
        num = Double.parseDouble(myformat.format(num));//保留2为小数
        System.out.println(num);
    }
}
