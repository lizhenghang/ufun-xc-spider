package com.ufun.bean;

/***
 * @author lizhenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/28 15:58
 */
public enum ErrorCode {

    A(2,"2:订单已过期"),B(3,"3:暂无可用代理"),C(-1,"-1:无效请求"),
    D(-2,"-2:订单无效。如果刚下单，请耐心等待一会儿，3分钟内系统会自动发货。"),
    E(-3,"-3:参数错误"),F(-4,"-4:提取失败: $err_msg"),G(-5,"-5:此订单不能提取私密代理。"),
    H(-51,"-51:此订单1分钟内允许最多$ip_number个ip调用"),
    I(-16,"-61:订单已退款"),J(-15,"-15:订单已过期"),K(-14,"-14:订单被封禁，请联系客服处理"),
    L(-13,"-13:订单已过期"),M(-12,"-12:订单无效"),N(-11,"-11:订单尚未支付"),
    O(-105,"-105:未知的签名方式"),P(-101,"-101:缺少参数：orderid"),Q(-102,"-102:订单无效或已过期"),
    R(-103,"-103:缺少签名参数"),S(-109,"-109:系统异常"),T(407,"407:用户密码验证缺失或错误"),
    U(503,"503:请求频率超过限制"),V(403,"403:IP超出最大数量限制（2小时内向超过10个网站post二进制数据会返回这个错误）");


    private Integer code;
    private String content;

    ErrorCode(Integer code,String content){
        this.code=code;
        this.content=content;
    }

    public static String getContent(Integer code){

        for (ErrorCode errorCode:ErrorCode.values()){

            if(errorCode.code==code){

                return errorCode.content;

            }

        }

        return null;
    }

    public static void main(String[] args) {
        System.out.println(ErrorCode.getContent(3));
    }
}
