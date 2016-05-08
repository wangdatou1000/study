package datou.core.exception;

import datou.core.exception.error.MyError;

/**
 *  服务调用的异常
 * Created by chunhua.zhang@yoho.cn on 2015/11/18.
 */
public class MyException extends RuntimeException{

    //服务错误码
	private final MyError myError;
    private final int code;
    private final String errorMessage;



    private String[] params;

    //是否输出堆栈
    private boolean printStack = false;

    /**
     * 服务异常。建议用 {@link #ServiceException(ServiceError)} 来构造
     * @param code 错误码
     * @param message 错误消息
     */
    public MyException(int code, String message) {
        this(code,message,false);
     }


    public MyException(int code, String message, boolean printStack) {
        this.code = code;
        this.errorMessage = message;
        this.printStack = printStack;

		MyError found = MyError.getErrorByCode(code);
		this.myError = found == null ? MyError.CODE_SUCCESS : found;
    }


    /**
     * 设置params。
     * @param params
     */
    public void setParams(String ... params){
        this.params = params;
    }
    public String[] getParams() {
        return params;
    }
    
	/**
	 * 服务异常
	 * 
	 * @param myError
	 *            服务异常
	 */
	public MyException(MyError myError) {
		this(myError, false);
    }


    /**
	 * 服务异常
	 * 
	 * @param myError
	 *            服务异常
	 */
	public MyException(MyError myError, boolean printStack) {
		this.myError = myError;
		this.code = myError.getCode();
		this.errorMessage = myError.getMessage();
        this.printStack = printStack;
    }
    
	/**
	 * 服务异常
	 * 
	 * @param Error
	 *            服务异常
	 * @param cause
	 *            服务异常
	 */

	public MyException(MyError myError, Throwable cause) {
		super(myError.getMessage(), cause);
		this.myError = myError;
		this.code = myError.getCode();
		this.errorMessage = myError.getMessage();
    }


    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return "[" + this.code + ":" + this.errorMessage + "]";
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public  boolean isPrintStack(){
        return this.printStack;
    }


}
