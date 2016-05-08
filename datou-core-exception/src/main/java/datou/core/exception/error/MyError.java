package datou.core.exception.error;

import java.util.Map;

/**
 *
 * Gateway的错误码定义 Created by on 2015/11/3.
 */
public enum MyError implements ErrorCode {



    // ----------------------- begin profile error ---------------------//

	CODE_SUCCESS(200),
	ERROR_500(500), ERROR_503(503);



    //-------------------------------------/


    static String DEFAULT_ERROR_MSG = "操作失败";
    static int DEFAULT_ERROR_CODE = 400;

    private final int code;
    private String message;

	MyError(int code)
    {
        this.code=code;
    }


    /**
     * 根据code查找错误
     * @param code code
     * @return error info
     */
	public static MyError getErrorByCode(int code) {
		for (MyError error : MyError.values()) {
           if(error.code == code){
               return error;
           }
       }
        return null;
    }



    @Override
    public int getCode() {
        return code;
    }

	public String getMessage(Object... param) {
		return message;
    }

    @Override
    public void setErrorContent(Map<String, Object> content) {

        this.message = (String)content.get("message");
    }
}
