package com.binfast.adpter.core.paramconvert;

import com.binfast.adpter.core.ParamsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class ParaProcessorBuilder {

	public static final ParaProcessorBuilder me = new ParaProcessorBuilder();
	private Map<Class<?>, Holder> typeMap = new HashMap<>();
	private static final Logger log = LoggerFactory.getLogger(ParaProcessorBuilder.class);
	private ParameterNameDiscoverer nameDiscoverer;

	private ParaProcessorBuilder() {
		regist(short.class, ShortGetter.class, "0");
		regist(int.class, IntegerGetter.class, "0");
		regist(long.class, LongGetter.class, "0");
		regist(float.class, FloatGetter.class, "0");
		regist(double.class, DoubleGetter.class, "0");
		regist(boolean.class, BooleanGetter.class, "false");
		regist(Short.class, ShortGetter.class, null);
		regist(Integer.class, IntegerGetter.class, null);
		regist(Long.class, LongGetter.class, null);
		regist(Float.class, FloatGetter.class, null);
		regist(Double.class, DoubleGetter.class, null);
		regist(Boolean.class, BooleanGetter.class, null);
		regist(String.class, StringGetter.class, null);
		regist(java.util.Date.class, DateGetter.class, null);
		regist(java.sql.Date.class, SqlDateGetter.class, null);
		regist(java.sql.Time.class, TimeGetter.class, null);
		regist(java.sql.Timestamp.class, TimestampGetter.class, null);
		regist(java.math.BigDecimal.class, BigDecimalGetter.class, null);
		regist(java.math.BigInteger.class, BigIntegerGetter.class, null);
		regist(java.io.File.class, FileGetter.class, null);
//		regist(com.jfinal.upload.UploadFile.class, UploadFileGetter.class, null);
		regist(String[].class, StringArrayGetter.class, null);
		regist(Integer[].class, IntegerArrayGetter.class, null);
		regist(Long[].class, LongArrayGetter.class, null);
		regist(RawData.class, RawDataGetter.class, null);
		
	}
	
	/**
	 * ?????????????????????????????????????????? 
	 * ParameterGetterBuilder.me().regist(java.lang.String.class, StringParaGetter.class, null);
	 * @param typeClass ??????????????? java.lang.Integer.class
	 * @param pgClass ???????????????????????????????????????ParaGetter
	 * @param defaultValue?????????????????????int???????????????0??? java.lang.Integer???????????????null
	 */
	public <T> void regist(Class<T> typeClass, Class<? extends ParaGetter<T>> pgClass, String defaultValue){
		this.typeMap.put(typeClass, new Holder(pgClass, defaultValue));
	}

	public ParaProcessor build(ParameterNameDiscoverer nameDiscoverer, Method method) {
		final int paraCount = method.getParameterCount();
		
		// ?????? action ???????????????????????????????????????????????? ParaProcessor ???????????? action??????????????? null ?????????
		if (paraCount == 0) {
			return NullParaProcessor.me;
		}
		
		ParaProcessor ret = new ParaProcessor(paraCount);
		
		String[] names = nameDiscoverer.getParameterNames(method);
		Parameter[] paras = method.getParameters();
		assert names != null;
		for (int i=0; i<paraCount; i++) {
			IParaGetter<?> pg = createParaGetter(method, paras[i], names[i]);
			ret.addParaGetter(i, pg);
		}
		
		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IParaGetter<?> createParaGetter(Method method, Parameter p, String parameterName) {
//		if(!p.isNamePresent()) {
//			log.warn("You should config compiler argument \"-parameters\" for parameter injection of action : " +
//					controllerClass.getName() + "." + method.getName() + "(...) \n" +
//					"Visit https://jfinal.com/doc/3-3 for details \n");
//		}
//		String parameterName = p.getName();
		String defaultValue = null;
		Class<?> typeClass = p.getType();
//		Para para = p.getAnnotation(Para.class);
//		if (para != null) {
//			// ?????? @Para ??????????????? defaultValue ????????????
//			if (!Para.NULL_VALUE.equals(para.value())) {
//				parameterName = para.value().trim();
//			}
//
//			/*
//			defaultValue = para.defaultValue().trim();
//			if (defaultValue.isEmpty()) {
//				defaultValue = null;
//			}*/
//			// ???????????? "" ????????????????????????????????????????????????????????????????????????????????????????????????: "  "???" ABC "
//			if (!Para.NULL_VALUE.equals(para.defaultValue())) {
//				defaultValue = para.defaultValue();
//			}
//
//		}
		Holder holder = typeMap.get(typeClass);
		if (holder != null) {
			if (null == defaultValue) {
				defaultValue = holder.getDefaultValue();
			}
			try {
				return holder.born(parameterName, defaultValue);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		//??????
		if(Enum.class.isAssignableFrom(typeClass)){
			return new EnumGetter(typeClass,parameterName,defaultValue);
		} else {
			return new BeanGetter(typeClass, parameterName, p);
		}
	}

	private static class Holder {
		private final String defaultValue;
		private final Class<? extends ParaGetter<?>> clazz;

		Holder(Class<? extends ParaGetter<?>> clazz, String defaultValue) {
			this.clazz = clazz;
			this.defaultValue = defaultValue;
		}
		final String getDefaultValue() {
			return defaultValue;
		}
		ParaGetter<?> born(String parameterName, String defaultValue) throws Exception {
			Constructor<? extends ParaGetter<?>> con = clazz.getConstructor(String.class, String.class);
			return con.newInstance(parameterName, defaultValue);
		}
	}
}
