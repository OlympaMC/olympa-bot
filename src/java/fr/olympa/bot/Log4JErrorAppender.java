package fr.olympa.bot;

import java.util.function.Consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

public class Log4JErrorAppender extends AbstractAppender {
	
	private Consumer<String> sendError;
	
	public Log4JErrorAppender(Consumer<String> sendError) {
		super("olympa-error-handler", null, null, false);
		this.sendError = sendError;
		
		setHandler(new CustomErrorHandler());
		setStarted();
	}
	
	@Override
	public void setHandler(ErrorHandler handler) {
		System.out.println("Log4JErrorAppender.setHandler() from " + getHandler().getClass().getName() + " to " + handler.getClass().getName());
		super.setHandler(handler);
	}
	
	@Override
	public void append(LogEvent record) {
		if (record.getThrown() != null) {
			String stackTrace = ExceptionUtils.getStackTrace(record.getThrown());
			sendError.accept(record.getLevel().name() + " [" + record.getLoggerName() + "] " + record + "\n" + stackTrace);
		}
	}
	
	public class CustomErrorHandler implements ErrorHandler {
		
		@Override
		public void error(String var1) {
			System.out.println("Log4JErrorAppender.CustomErrorHandler.error(String)");
			sendError.accept(var1);
		}
		
		@Override
		public void error(String var1, Throwable var2) {
			System.out.println("Log4JErrorAppender.CustomErrorHandler.error(String, Throwable)");
			sendError.accept(var1 + " " + ExceptionUtils.getStackTrace(var2));
		}
		
		@Override
		public void error(String var1, LogEvent record, Throwable var3) {
			System.out.println("Log4JErrorAppender.CustomErrorHandler.error(String, LogEvent, Throwable)");
			String stackTrace = ExceptionUtils.getStackTrace(var3);
			sendError.accept(record.getLevel().name() + " [" + record.getLoggerName() + "] " + var1 + "\n" + stackTrace);
		}
		
	}
	
}
