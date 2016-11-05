package org.voovan.tools;


import javafx.print.Collation;
import org.omg.CORBA.OBJ_ADAPTER;

import java.lang.reflect.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * 反射工具类
 * 
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class TReflect {

	/**
	 * 获得类所有的Field
	 * 
	 * @param clazz 类对象
	 * @return Field数组
	 */
	public static Field[] getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		for( ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
			Field[] tmpFields = clazz.getDeclaredFields();
			fields.addAll(Arrays.asList(tmpFields));
		}
		return fields.toArray(new Field[]{});
	}

	/**
	 * 查找类特定的Field
	 * 
	 * @param clazz   类对象
	 * @param fieldName field 名称
	 * @return field 对象
	 * @throws NoSuchFieldException 无 Field 异常
	 * @throws SecurityException 安全性异常
	 */
	public static Field findField(Class<?> clazz, String fieldName)
			throws ReflectiveOperationException {
		try {
			return clazz.getDeclaredField(fieldName);
		}catch(NoSuchFieldException ex){
			Class superClazz = clazz.getSuperclass();
			if( superClazz != Object.class ) {
				return findField(clazz.getSuperclass(), fieldName);
			}else{
				return null;
			}
		}
	}

	/**
	 * 查找类特定的Field
	 * 			不区分大小写,并且替换掉特殊字符
	 * @param clazz   类对象
	 * @param fieldName Field 名称
	 * @return Field 对象
	 * @throws ReflectiveOperationException 反射异常
     */
	public static Field findFieldIgnoreCase(Class<?> clazz, String fieldName)
			throws ReflectiveOperationException{
		for(Field field : getFields(clazz)){
			if(field.getName().equalsIgnoreCase(fieldName)){
				return field;
			}
		}
		return null;
	}

	/**
	 * 获取 Field 的范型类型
	 * @param field  field 对象
	 * @return 返回范型类型数组
	 * @throws ClassNotFoundException 类找不到异常
	 */
	public static Class[] getFieldGenericType(Field field) throws ClassNotFoundException {
		Class[] result = null;
		Type fieldType = field.getGenericType();
		if(fieldType instanceof ParameterizedType){
			ParameterizedType parameterizedFieldType = (ParameterizedType)fieldType;
			Type[] actualType = parameterizedFieldType.getActualTypeArguments();
			result = new Class[actualType.length];

			for(int i=0;i<actualType.length;i++){
				result[i] = Class.forName(actualType[i].getTypeName());
			}
			return result;
		}
		return null;
	}

	/**
	 * 获取类中指定Field的值
	 * @param <T> 范型
	 * @param obj  对象
	 * @param fieldName Field 名称
	 * @return Field 的值
	 * @throws ReflectiveOperationException 反射异常
	 */
	@SuppressWarnings("unchecked")
	static public <T> T getFieldValue(Object obj, String fieldName)
			throws ReflectiveOperationException {
		Field field = findField(obj.getClass(), fieldName);
		field.setAccessible(true);
		return (T) field.get(obj);
	}

	/**
	 * 更新对象中指定的Field的值
	 * 		注意:对 private 等字段有效
	 * 
	 * @param obj  对象
	 * @param fieldName field 名称
	 * @param fieldValue field 值
	 * @throws ReflectiveOperationException 反射异常
	 */
	public static void setFieldValue(Object obj, String fieldName,
			Object fieldValue) throws ReflectiveOperationException {
		Field field = findField(obj.getClass(), fieldName);
		field.setAccessible(true);
		field.set(obj, fieldValue);
	}

	/**
	 * 将对象中的field和其值组装成Map 静态字段(static修饰的)不包括
	 * 
	 * @param obj 对象
	 * @return 所有 field 名称-值拼装的 Map
	 * @throws ReflectiveOperationException 反射异常
	 */
	public static Map<Field, Object> getFieldValues(Object obj)
			throws ReflectiveOperationException {
		HashMap<Field, Object> result = new HashMap<Field, Object>();
		Field[] fields = getFields(obj.getClass());
		for (Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				Object value = getFieldValue(obj, field.getName());
				result.put(field, value);
			}
		}
		return result;
	}

	/**
	 * 查找类中的方法
	 * @param clazz        类对象
	 * @param name		   方法名	
	 * @param paramTypes   参数类型
	 * @return			   方法对象
	 * @throws ReflectiveOperationException 反射异常
	 */
	public static Method findMethod(Class<?> clazz, String name,
			Class<?>... paramTypes) throws ReflectiveOperationException  {
		return clazz.getDeclaredMethod(name, paramTypes);
	}
	
	/**
	 * 获取类的方法集合
	 * @param clazz		类对象
	 * @return Method 对象数组
	 */
	public static Method[] getMethods(Class<?> clazz) {
		List<Method> methods = new ArrayList<Method>();
		for( ; clazz != Object.class ; clazz = clazz.getSuperclass()) {
			Method[] tmpMethods = clazz.getDeclaredMethods();
			methods.addAll(Arrays.asList(tmpMethods));
		}
		return methods.toArray(new Method[]{});
	}
	
	/**
	 * 获取类的特定方法的集合
	 * 		类中可能存在同名方法
	 * @param clazz		类对象
	 * @param name		方法名	
	 * @return Method 对象数组
	 */
	public static Method[] getMethods(Class<?> clazz,String name) {
		ArrayList<Method> methods = new ArrayList<Method>();
		Method[] allMethod = getMethods(clazz);
		
		for(Method method : allMethod){
			if(method.getName().equals(name) )
			methods.add(method);
		}
		return methods.toArray(new Method[0]);
	}

	/**
	 * 获取方法的参数和返回值的范型类型
	 * @param method  method 对象
	 * @param parameterIndex 参数索引 (>0) 参数索引位置[第一个参数为0,以此类推], (-1) 返回值
	 * @return 返回范型类型数组
	 * @throws ClassNotFoundException 类找不到异常
	 */
	public static Class[] getMethodParameterGenericType(Method method,int parameterIndex) throws ClassNotFoundException {
		Class[] result = null;
		Type parameterType;

		if(parameterIndex == -1){
			parameterType = method.getGenericReturnType();
		}else{
			parameterType = method.getGenericParameterTypes()[parameterIndex];
		}

		if(parameterType instanceof ParameterizedType){
			ParameterizedType parameterizedFieldType = (ParameterizedType)parameterType;
			Type[] actualType = parameterizedFieldType.getActualTypeArguments();
			result = new Class[actualType.length];

			for(int i=0;i<actualType.length;i++){
				result[i] = Class.forName(actualType[i].getTypeName());
			}
			return result;
		}
		return null;
	}
	
	/**
	 * 使用对象执行它的一个方法
	 * 		对对象执行一个指定Method对象的方法
	 * @param obj				执行方法的对象
	 * @param method			方法对象
	 * @param parameters        多个参数
	 * @return					方法返回结果
	 * @throws ReflectiveOperationException 反射异常
	 */
	public static Object invokeMethod(Object obj, Method method, Object... parameters)
			throws ReflectiveOperationException {
		method.setAccessible(true);
		return method.invoke(obj, parameters);
	}

	/**
	 * 使用对象执行方法
	 * 对对象执行一个通过 方法名和参数列表选择的方法
	 * @param obj				执行方法的对象
	 * @param name				执行方法名
	 * @param parameters		方法参数
	 * @return					方法返回结果
	 * @throws ReflectiveOperationException		反射异常
	 */
	public static Object invokeMethod(Object obj, String name, Object... parameters)
			throws ReflectiveOperationException {
		Class<?>[] parameterTypes = getArrayClasses(parameters);
		Method method;
		try {
			 method = findMethod(obj.getClass(), name, parameterTypes);
		}catch(NoSuchMethodException e){
			for(int i=0; i<parameterTypes.length; i++){
				parameterTypes[i] = convertNativeType(parameterTypes[i]);
			}
			 method = findMethod(obj.getClass(), name, parameterTypes);
		}
		method.setAccessible(true);
		return method.invoke(obj, parameters);
	}

	/**
	 * 转换基本类型的对象类型到基本类型
	 * @param clazz 基本类型的对象类型
	 * @return 基本类型
	 */
	public static Class convertNativeType(Class clazz){
		if(clazz == Integer.class){
			return int.class;
		}

		if(clazz == Long.class){
			return long.class;
		}

		if(clazz == Short.class){
			return short.class;
		}

		if(clazz == Float.class){
			return float.class;
		}

		if(clazz == Double.class){
			return double.class;
		}

		if(clazz == Boolean.class){
			return boolean.class;
		}

		if(clazz ==Character.class){
			return char.class;
		}

		if(clazz == Byte.class){
			return byte.class;
		}

		return clazz;
	}

	/**
	 * 构造新的对象
	 * 	通过参数中的构造参数对象parameters,选择特定的构造方法构造
	 * @param <T>           范型
	 * @param clazz			类对象
	 * @param parameters	构造方法参数
	 * @return 新的对象
	 * @throws ReflectiveOperationException 反射异常
	 */
	public static <T> T newInstance(Class<T> clazz, Object ...parameters)
			throws ReflectiveOperationException {
		Class<?>[] parameterTypes = getArrayClasses(parameters);
		Constructor<T> constructor = null;
		if(parameters.length==0){
			constructor = clazz.getConstructor();
		}else {
			constructor = clazz.getConstructor(parameterTypes);
		}
		return constructor.newInstance(parameters);
	}
	
	/**
	 * 构造新的对象
	 * @param <T> 范型
	 * @param className		类名称
	 * @param parameters	构造方法参数
	 * @return 新的对象
	 * @throws ReflectiveOperationException 反射异常
	 */
	public static <T> T newInstance(String className, Object ...parameters) throws ReflectiveOperationException {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) Class.forName(className);
		return newInstance(clazz,parameters);
	}
	
	/**
	 * 将对象数组转换成,对象类型的数组
	 * @param objs	对象类型数组
	 * @return 类数组
	 */
	public static Class<?>[] getArrayClasses(Object[] objs){
		Class<?>[] parameterTypes= new Class<?>[objs.length];
		for(int i=0;i<objs.length;i++){
			parameterTypes[i] = objs[i].getClass();
		}
		return parameterTypes;
	}

	/**
	 * 将Map转换成指定的对象
	 * 
	 * @param clazz			类对象
	 * @param mapArg		Map 对象
	 * @param ignoreCase    匹配属性名是否不区分大小写
	 * @return 转换后的对象
	 * @throws ReflectiveOperationException 反射异常
	 * @throws ParseException 解析异常
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getObjectFromMap(Class<?> clazz,
		Map<String, ?> mapArg,boolean ignoreCase) throws ReflectiveOperationException, ParseException {
		Object obj = null;

		if(mapArg==null){
			return obj;
		}

		//由于 List 和 Map 无法构造, 所以使用默认类型
		if(clazz == List.class){
			clazz= ArrayList.class;
		}else if(clazz == Map.class){
			clazz= HashMap.class;
		}

		if(mapArg.isEmpty()){

			return TReflect.newInstance(clazz,new Object[]{});
		}

		Object singleValue = mapArg.values().iterator().next();
		// java标准对象
		if (clazz.isPrimitive() || clazz == Object.class){
			obj = singleValue;
		}
		//java 日期对象
		else if(isExtendsByClass(clazz,Date.class)){
			//取 Map.Values 里的递第一个值
			String value = singleValue==null?null:singleValue.toString();
			SimpleDateFormat dateFormat = new SimpleDateFormat(TDateTime.STANDER_DATETIME_TEMPLATE);
			obj = singleValue!=null?dateFormat.parse(value.toString()):null;
		}
		//Map 类型
		else if(isImpByInterface(clazz,Map.class)){
			Map mapObject = TObject.cast(newInstance(clazz));
			mapObject.putAll(mapArg);
			obj = mapObject;
		}
		//Collection 类型
		else if(isImpByInterface(clazz,Collection.class)){
            Collection listObject = TObject.cast(newInstance(clazz));
			if(singleValue!=null){
                listObject.addAll((Collection) TObject.cast(singleValue));
			}
			obj = listObject;
		}
		//java基本对象
		else if (clazz.getName().startsWith("java.lang")) {
			//取 Map.Values 里的递第一个值
			String value = singleValue==null?null:singleValue.toString();
			obj = singleValue==null?null:newInstance(clazz,  value);
		}
		// 复杂对象
		else {
			obj = newInstance(clazz);
			for(Entry<String,?> argEntry : mapArg.entrySet()){
				String key = argEntry.getKey();
				Object value = argEntry.getValue();

				Field field = null;
				if(ignoreCase) {
					//忽略大小写匹配
					field = findFieldIgnoreCase(clazz, key);
				}else{
					//精确匹配属性
					field = findField(clazz, key);
				}

				if(field!=null) {
					String fieldName = field.getName();
					Class<?> fieldType = field.getType();

					//对于 对象类型为 Map 的属性进行处理,查找范型,并转换为范型定义的类型
					if(isImpByInterface(fieldType,Map.class) && value instanceof Map){
						Class[] mapGenericTypes = getFieldGenericType(field);
						if(mapGenericTypes!=null) {
							HashMap result = new HashMap();
							Map mapValue = (Map) value;
							Iterator iterator = mapValue.entrySet().iterator();
							while(iterator.hasNext() ) {
								Entry entry = (Entry) iterator.next();
								Map keyMap = null;
								Map valueMap = null;
								if(entry.getKey() instanceof Map){
									keyMap = (Map)entry.getKey();
								}else{
									keyMap = TObject.newMap("value",entry.getKey());
								}

								if(entry.getValue() instanceof Map){
									valueMap = (Map)entry.getValue();
								}else{
									valueMap = TObject.newMap("value",entry.getValue());
								}

								Object keyObj = getObjectFromMap(mapGenericTypes[0], keyMap, ignoreCase);
								Object valueObj = getObjectFromMap(mapGenericTypes[1], valueMap, ignoreCase);
								result.put(keyObj, valueObj);
							}
							mapValue.clear();
							mapValue.putAll(result);
						}
					}
					//对于 对象类型为 Collection 的属性进行处理,查找范型,并转换为范型定义的类型
					else if(isImpByInterface(fieldType,Collection.class) && value instanceof Collation){
						Class[] listGenericTypes = getFieldGenericType(field);
						if(listGenericTypes!=null) {
							ArrayList result = new ArrayList();
							List listValue = (List)value;
							for(Object listItem : listValue){
								Map valueMap = null;
								if(listValue instanceof Map){
									valueMap = (Map)listValue;
								}else{
									valueMap = TObject.newMap("value",listValue);
								}

								Object item = getObjectFromMap(listGenericTypes[0],valueMap,ignoreCase);
								result.add(item);
							}
							listValue.clear();
							listValue.addAll(result);
						}

					}else if(value instanceof Map){
						value = getObjectFromMap(fieldType,(Map<String, ?>)value, ignoreCase);

					} else {
						value = getObjectFromMap(fieldType, TObject.newMap("value", value), ignoreCase);
					}

					setFieldValue(obj, fieldName, value);
				}
			}
		}
		return obj;
	}

	/**
	 * 将对象转换成 Map
	 * 			key 对象属性名称
	 * 			value 对象属性值
	 * @param obj      待转换的对象
	 * @return 转后的 Map
	 * @throws ReflectiveOperationException 反射异常
	 */
	public static Map<String, Object> getMapfromObject(Object obj) throws ReflectiveOperationException{
		
		Map<String, Object> mapResult = new HashMap<String, Object>();
		Map<Field, Object> fieldValues =  TReflect.getFieldValues(obj);
		//如果是 java 标准类型
		if(obj.getClass().getName().startsWith("java.lang")
				|| obj.getClass().isPrimitive()){
			mapResult.put("value", obj);
		}
		//对 Collection 类型的处理
		else if(obj instanceof Collection){
			Collection collection = (Collection) newInstance(obj.getClass());
			for(Object collectionItem : (Collection)obj) {
				collection.add(getMapfromObject(collectionItem));
			}
			mapResult.put("value", collection);
		}
		//对 Map 类型的处理
		else if(obj instanceof Map){
			Map mapObject = (Map)obj;
			Map map = (Map)newInstance(obj.getClass());
			for(Object key : mapObject.keySet()) {
				map.put(getMapfromObject(key),getMapfromObject(mapObject.get(key)));
			}
			mapResult.put("value", map);
		}
		//复杂对象类型
		else{
			for(Entry<Field,Object> entry : fieldValues.entrySet()){
				String key = entry.getKey().getName();
				Object value = entry.getValue();
				if(value == null){
					mapResult.put(key, value);
				}else if(!key.contains("$")){
					String valueClass = entry.getValue().getClass().getName();
					if(valueClass.startsWith("java")){
						mapResult.put(key, value);
					}else {
						//如果是复杂类型则递归调用
						Map resultMap = getMapfromObject(value);
						if(resultMap.size()==1 && resultMap.containsKey("value")){
							mapResult.put(key, resultMap.values().iterator().next());
						}else{
							mapResult.put(key,resultMap);
						}

					}
				}
			}
		}
		return mapResult;
	}
	
	/**
	 * 判断某个类型是否实现了某个接口
	 * 		包括判断其父接口
	 * @param type               被判断的类型
	 * @param interfaceClass     检查是否实现了次类的接口
	 * @return 是否实现某个接口
	 */
	public static boolean isImpByInterface(Class<?> type,Class<?> interfaceClass){
		if(type==interfaceClass){
			return true;
		}

		Class<?>[] interfaces= type.getInterfaces();
		for (Class<?> interfaceItem : interfaces) {
			if (interfaceItem.equals(interfaceClass)) {
				return true;
			}
			else{
				return isImpByInterface(interfaceItem,interfaceClass);
			}
		}
		return false;
	}
	
	/**
	 * 判断某个类型是否继承于某个类
	 * 		包括判断其父类
	 * @param type			判断的类型
	 * @param extendsClass	用于判断的父类类型
	 * @return 是否继承于某个类
	 */
	public static boolean isExtendsByClass(Class<?> type,Class<?> extendsClass){
		if(extendsClass == type){
			return true;
		}

		Class<?> superClass = type;
		do{
			if(superClass.equals(extendsClass)){
				return true;
			}
			superClass = superClass.getSuperclass();
		}while(superClass!=null && !superClass.equals(extendsClass) && !superClass.equals(Object.class));

		return false;
	}

	/**
	 * 获取类的 json 形式的描述
	 * @param clazz  Class 类型对象
	 * @return 类的 json 形式的描述
     */
	public static String getClazzJSONModel(Class clazz){
		StringBuilder jsonStrBuilder = new StringBuilder();
		if(clazz.getName().startsWith("java") || clazz.isPrimitive()){
			jsonStrBuilder.append(clazz.getName());
		} else if(clazz.getName().startsWith("[L")){
			String clazzName = clazz.getName();
			clazzName = clazzName.substring(clazzName.lastIndexOf(".")+1,clazzName.length()-2)+"[]";
			jsonStrBuilder.append(clazzName);
		} else {
			jsonStrBuilder.append("{");
			for (Field field : TReflect.getFields(clazz)) {
				jsonStrBuilder.append("\"");
				jsonStrBuilder.append(field.getName());
				jsonStrBuilder.append("\":");
				String filedValueModel = getClazzJSONModel(field.getType());
				if(filedValueModel.startsWith("{") && filedValueModel.endsWith("}")) {
					jsonStrBuilder.append(filedValueModel);
					jsonStrBuilder.append(",");
				} else if(filedValueModel.startsWith("[") && filedValueModel.endsWith("]")) {
					jsonStrBuilder.append(filedValueModel);
					jsonStrBuilder.append(",");
				} else {
					jsonStrBuilder.append("\"");
					jsonStrBuilder.append(filedValueModel);
					jsonStrBuilder.append("\",");
				}
			}
			jsonStrBuilder.deleteCharAt(jsonStrBuilder.length()-1);
			jsonStrBuilder.append("}");
		}

		return jsonStrBuilder.toString();
	}
}
