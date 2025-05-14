package hh.javabasicsexample.com;

import java.math.BigDecimal;

/**
 * @author HuangHao
 * @since 2025/5/14 14:00
 * @des BigDecimal避坑
 */
public class BigDecimalDemo {

    public static void main(String[] args) {
        //1. 使用BigDecimal的构造函数传入浮点数
        BigDecimal bigDecimal = new BigDecimal(1);
        BigDecimal bigDecima2 = new BigDecimal(0.9);
        //输出结果：0.09999999999999997779553950749686919152736663818359375
        System.out.println(bigDecimal.subtract(bigDecima2));
        //改进使用字符串传入或BigDecimal.valueOf
        BigDecimal bigDecimal1 = BigDecimal.valueOf(1);
        BigDecimal bigDecimal2 = BigDecimal.valueOf(0.9);
        //输出结果：0.1
        System.out.println(bigDecimal1.subtract(bigDecimal2));
        BigDecimal bigDecimal3 = new BigDecimal("1");
        BigDecimal bigDecimal4 = new BigDecimal("0.9");
        //输出结果：0.1
        System.out.println(bigDecimal3.subtract(bigDecimal4));
        //2. 使用equals()方法进行数值比较
        BigDecimal bigDecimal5 = new BigDecimal("0.01");
        BigDecimal bigDecimal6 = new BigDecimal("0.010");
        //会比较数值的精度
        System.out.println(bigDecimal5.equals(bigDecimal6));
        //直接比较值
        System.out.println(bigDecimal5.compareTo(bigDecimal6));
        //3. 使用不正确的舍入模式
        BigDecimal bigDecimal7 = new BigDecimal("1.0");
        BigDecimal bigDecimal8 = new BigDecimal("3.0");
        //java.lang.ArithmeticException: Non-terminating decimal expansion; no exact representable decimal result.
        System.out.println(bigDecimal7.divide(bigDecimal8));
        //四舍五入保留二位小数：0.33
        System.out.println(bigDecimal7.divide(bigDecimal8, 2, BigDecimal.ROUND_HALF_UP));
    }
}
