package cn.yuyizyk.compent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import cn.yuyizyk.action.BaseAction;
import cn.yuyizyk.util.Objs;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

/**
 * 
 * 
 * 
 *
 * @author yuyi
 */
public class SrcActionAnalysis {
	public static class AppClassLoader extends ClassLoader {

		public AppClassLoader(ClassLoader pa) {
			super(pa);
		}

		/**
		 * 通过classBytes加载类
		 *
		 * @param className
		 * @param classBytes
		 * @return
		 */
		public Class<?> findClassByBytes(String className, byte[] classBytes) {
			return defineClass(className, classBytes, 0, classBytes.length);
		}
	}

	private final static Logger log = LoggerFactory.getLogger(SrcActionAnalysis.class);

	/**
	 * 复制对象所有属性值,并返回一个新对象
	 *
	 * @param srcObj
	 * @param handle
	 * @param m
	 * @return
	 */
	private static Object getObj(Class<?> clazz, Object srcObj, Class<?>[] clzs, Object[] clzConstructorArgs) {
		try {
			Object newInstance = clazz.getConstructor(clzs).newInstance(clzConstructorArgs);
			Field[] fields = srcObj.getClass().getDeclaredFields();
			for (Field oldInstanceField : fields) {
				if ((oldInstanceField.getModifiers() & Modifier.STATIC) != 0)
					continue;
				String fieldName = oldInstanceField.getName();
				oldInstanceField.setAccessible(true);
				Field newInstanceField = findField(newInstance.getClass(), fieldName, oldInstanceField.getType());
				newInstanceField.setAccessible(true);
				newInstanceField.set(newInstance, oldInstanceField.get(srcObj));
			}
			return newInstance;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据baseAction 命名约定，返回默认url
	 * 
	 * @param t
	 * @return
	 */
	private static String getUriClass(Class<?> t) {
		String urlc = t.getSimpleName().endsWith("Action")
				? t.getSimpleName().substring(0, t.getSimpleName().length() - "Action".length())
				: t.getSimpleName().endsWith("Api")
						? t.getSimpleName().substring(0, t.getSimpleName().length() - "Api".length())
						: t.getSimpleName();

		return new StringBuilder().append(Character.toLowerCase(urlc.charAt(0))).append(urlc.substring(1)).toString();
	}

	/**
	 * 注册action 到RequestMappingHandlerMapping中
	 * <p>
	 * 将baseaction（非spring copment） 的子类实例中的每一个非static 方法注册为一个handlaction
	 * 到RequestMappingHandlerMapping 用以spring 使用。 <br/>
	 * handlaction默认以包名，类名（首字母小写），方法名作为uri<br/>
	 * 当方法存在注解 RequestMapping 时，以RequestMapping等注解 值为uri
	 * （目前不匹配collect上RequestMapping）<br/>
	 * </p>
	 * 
	 * <pre>
	 * 	 ...action.PascAction.share2()				= pacs.share2
	 *   &#64;RequestMapping("shr") ...action.PascAction.share2()		= shr
	 * </pre>
	 * 
	 * 关于方法参数：目前实现方式为在动态新增的handleraction 中加入注解RequestParam 作为默认无注解形参定义。
	 * 
	 * @param requestMappingHandlerMapping
	 * @param c
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void registerMapping(RequestMappingHandlerMapping requestMappingHandlerMapping, Class<?> c) {
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
		ClassPool pool = ClassPool.getDefault();
		AppClassLoader appClassLoader = new AppClassLoader(Thread.currentThread().getContextClassLoader());
		try {
			pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
			// pool.appendClassPath(SystemConstants.getClassPath().toString());
			// pool.appendClassPath(new
			// LoaderClassPath(ServletRequest.class.getClassLoader()));
			if (BaseAction.class.isAssignableFrom(c) && (c.getModifiers() & Modifier.ABSTRACT) == 0) {
				log.debug("registerMapping initialize {}...   ", c);
				String actionName;
				StringBuilder pageName = new StringBuilder().append(StringUtils.replaceChars(
						c.getPackage().getName().replaceAll(BaseAction.class.getPackage().getName(), ""), '.', '/'));
				StringBuilder firstPath = new StringBuilder().append(pageName).append("/")
						.append(actionName = getUriClass(c));
				Stream.of(c.getMethods()).filter(m -> m.getDeclaringClass().equals(c))
						.filter(m -> (m.getModifiers() & Modifier.STATIC) == 0).forEach(m -> {
							try {
								RequestMappingInfo.Builder minfoB = SrcActionAnalysis.getRequestMappingInfoBuilder(c, m,
										actionName, pageName, firstPath);
								HandlerAction<? extends BaseAction> handleAction = new HandlerAction<>(
										(Class<? extends BaseAction>) c, m);
								Handler obj;
								Method newMethod;
								// if (Objs.isEmpty(m.getParameterTypes())) {
								// obj = handle;
								// newMethod = HandlerAction.class.getMethod("run", ServletRequest.class,
								// ServletResponse.class);
								// } else {

								CtClass handleCtClz = pool
										.makeClass(
												Handler.class.getName() + "$" + c.getSimpleName() + "$" + m.getName()
														+ "R" + (new Random().nextInt(1024)),
												pool.get(Handler.class.getName()));
								// CtClass srcCtClz = pool.get(c.getName());
								// CtMethod srcCtMethod = srcCtClz.getDeclaredMethod(m.getName(),
								// pool.get(Stream
								// .of(m.getParameterTypes()).map(c2 -> c2.getName()).toArray(String[]::new)));
								// 设置方法名 修饰符 返回类型
								StringBuilder sb = new StringBuilder();
								sb.append(Modifier.toString(m.getModifiers())).append(" ")
										.append(m.getReturnType().getName()).append(" ").append(m.getName())
										.append("(");

								// 设置参数
								List<String> pars = new ArrayList<>();
								for (int i = 0; i < m.getParameterCount(); i++) {
									StringBuilder sb2 = new StringBuilder();
									sb2.append(m.getParameterTypes()[i].getName()).append(" ")
											.append(u.getParameterNames(m)[i]);
									pars.add(sb2.toString());
								}
								pars.add("javax.servlet.ServletRequest req");
								pars.add("javax.servlet.ServletResponse resp");

								sb.append(StringUtils.join(pars, " , ")).append(")").append("\n throws Throwable ")
										// .append(StringUtils.join(Stream.of(newMethod.getExceptionTypes()).map(Class::getName),","))
										.append("{\n");

								if (!m.getReturnType().getName().equals("void")) {
									sb.append("return  (").append(m.getReturnType().getName()).append(") ");

								}
								sb.append("invoke(req, resp, ");
								if (m.getParameterCount() == 0)
									sb.append("new Object[0]");
								else
									sb.append("new Object[] {").append(StringUtils.join(u.getParameterNames(m), " , "))
											.append("}");
								sb.append(");\n }");
								ClassFile ccFile = handleCtClz.getClassFile();
								ConstPool constpool = ccFile.getConstPool();

								AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool,
										AnnotationsAttribute.visibleTag);

								CtMethod newCtMethod = CtNewMethod.make(sb.toString(), handleCtClz);
								MethodInfo info = newCtMethod.getMethodInfo();
								info.addAttribute(methodAttr);
								// info.addAttribute(methodPar);

								// method 注解拷贝
								for (Annotation a : m.getAnnotations()) {
									Decorator: {
										if (a.annotationType().equals(Transactional.class)) {
											handleAction = new TransactionHandlerAction<>(handleAction);
											break Decorator;
										}
										// if (a.annotationType().equals(SelectNative.class)) {
										// handleAction = new CanelI18NHandlerAction(handleAction);
										// break Decorator;
										// }
										// if (a.annotationType().equals(CancelXSS.class)) {
										// handleAction = new CancelXssHandlerAction<>(handleAction);
										// break Decorator;
										// }
									}
									methodAttr.addAnnotation(
											toJavassistAnnotation(a.annotationType().getName(), a, constpool));
								}
								ParameterAnnotationsAttribute paa = new ParameterAnnotationsAttribute(constpool,
										ParameterAnnotationsAttribute.visibleTag);

								javassist.bytecode.annotation.Annotation[][] nAs = new javassist.bytecode.annotation.Annotation[m
										.getParameterCount() + 2][];
								Annotation[][] as = m.getParameterAnnotations();
								for (int i = 0; i < as.length; i++) {
									if (as[i].length == 0) {
										nAs[i] = new javassist.bytecode.annotation.Annotation[1];
										nAs[i][0] = getDefaultRequestParam(u.getParameterNames(m)[i], constpool);
									} else {
										nAs[i] = new javassist.bytecode.annotation.Annotation[as[i].length];
										for (int j = 0; j < as[i].length; j++) {
											nAs[i][j] = toJavassistAnnotation(as[i][j].annotationType().getName(),
													as[i][j], constpool);
										}
									}
								}

								nAs[nAs.length - 2] = new javassist.bytecode.annotation.Annotation[0];
								nAs[nAs.length - 1] = new javassist.bytecode.annotation.Annotation[0];
								// nAs[nAs.length - 1][0] = getDefaultRequestParam("resp", constpool);
								paa.setAnnotations(nAs);

								info.addAttribute(paa);

								handleCtClz.addMethod(newCtMethod);
								// CtConstructor ctConstructor = new CtConstructor(new CtClass[] {
								// pool.get(Class.class.getName()), pool.get(Method.class.getName()) },
								// handleCtClz);
								// handleCtClz.addConstructor(ctConstructor);

								Class<?> clazz = appClassLoader.findClassByBytes(handleCtClz.getName(),
										handleCtClz.toBytecode());
								// TODO 取消写classfile
								// handleCtClz.writeFile("");
								// handleCtClz.writeFile(new
								// ClassPathResource("").getFile().toPath().toString());

								obj = new Handler(handleAction);

								obj = (Handler) getObj(clazz, obj, new Class[] { HandlerAction.class },
										new Object[] { handleAction });
								List<Class<?>> li = new ArrayList<>();
								Stream.of(m.getParameterTypes()).forEach(li::add);
								li.add(ServletRequest.class);
								li.add(ServletResponse.class);

								newMethod = clazz.getMethod(m.getName(), li.toArray(new Class<?>[] {}));
								// }
								requestMappingHandlerMapping.registerMapping(minfoB.build(), obj, newMethod);

							} catch (Exception e) {
								// TODO: handle exception
								log.error("", e);
								System.out.println(pool.getClassLoader());
							}
						});
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private static final javassist.bytecode.annotation.Annotation getDefaultRequestParam(String name,
			ConstPool constpool) {
		javassist.bytecode.annotation.Annotation anno = new javassist.bytecode.annotation.Annotation(
				RequestParam.class.getName(), constpool);
		anno.addMemberValue("value", new StringMemberValue(name, constpool));
		anno.addMemberValue("required", new BooleanMemberValue(false, constpool));
		return anno;
	}

	/**
	 * 获得方法定义的字符串
	 * 
	 * @param clz
	 * @param m
	 * @return
	 * @throws NotFoundException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unused")
	private static final String getProxyMethodStr(Class<?> clz, Method m)
			throws NotFoundException, ClassNotFoundException {
		ClassPool pool = ClassPool.getDefault();
		CtClass handleCtClz = pool.get(clz.getName());
		CtMethod srcCtMethod = handleCtClz.getDeclaredMethod(m.getName(),
				pool.get(Stream.of(m.getParameterTypes()).map(c -> c.getName()).toArray(String[]::new)));
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();

		StringBuilder sb = new StringBuilder();
		for (Object a : srcCtMethod.getAnnotations())
			sb.append(a.toString()).append("\n");
		sb.append(Modifier.toString(m.getModifiers())).append(" ").append(srcCtMethod.getReturnType().getName())
				.append(" ").append(m.getName()).append("(");

		List<String> pars = new ArrayList<>();
		for (int i = 0; i < m.getParameterCount(); i++) {
			StringBuilder sb2 = new StringBuilder();
			for (Object a : srcCtMethod.getParameterAnnotations()[i])
				sb2.append(a.toString()).append(" ");

			sb2.append(m.getParameterTypes()[i].getName()).append(" ").append(u.getParameterNames(m)[i]);
			pars.add(sb2.toString());
		}
		// TO DO 打印 Throwable
		sb.append(StringUtils.join(pars, " , ")).append(")").append("\n throws Throwable ").append("{\n");

		if (!srcCtMethod.getReturnType().getName().equals("void")) {
			sb.append("return  (").append(srcCtMethod.getReturnType().getName()).append(") ");

		}
		// TODO BODY
		sb.append("method.invoke(init(req, resp), new Object[] {")
				.append(StringUtils.join(u.getParameterNames(m), " , ")).append("});").append("\n }");
		return sb.toString();
	}

	/**
	 * 获得对应的MemberValue 实例
	 * 
	 * @param clz
	 *            对象类型
	 * @param resul
	 *            对象值
	 * @param cp
	 * @return
	 * @throws NotFoundException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("rawtypes")
	private static final MemberValue getMemberValue(Class<?> clz, Object resul, ConstPool cp)
			throws NotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		switch (clz.getName()) {
		case "boolean":
			if (resul != null)
				return new BooleanMemberValue((boolean) resul, cp);
			return new BooleanMemberValue(cp);
		case "int":
			if (resul != null)
				return new IntegerMemberValue((int) resul, cp);
			return new IntegerMemberValue(cp);
		case "float":
			if (resul != null)
				return new FloatMemberValue((float) resul, cp);
			return new FloatMemberValue(cp);
		case "long":
			if (resul != null)
				return new LongMemberValue((long) resul, cp);
			return new LongMemberValue(cp);
		case "double":
			if (resul != null)
				return new DoubleMemberValue((double) resul, cp);
			return new DoubleMemberValue(cp);
		case "byte":
			if (resul != null)
				return new ByteMemberValue((byte) resul, cp);
			return new ByteMemberValue(cp);
		case "char":
			if (resul != null)
				return new CharMemberValue((char) resul, cp);
			return new CharMemberValue(cp);
		case "short":
			if (resul != null)
				return new ShortMemberValue((short) resul, cp);
			return new ShortMemberValue(cp);
		case "java.lang.Class":
			if (resul != null)
				return new ClassMemberValue(((Class<?>) resul).getName(), cp);
			return new ClassMemberValue(cp);
		case "java.lang.String":
			if (resul != null)
				return new StringMemberValue((String) resul, cp);
			return new StringMemberValue(cp);
		}
		if (clz.isArray()) {
			List<MemberValue> mv = new ArrayList<>();
			for (Object obj : ((Object[]) resul)) {
				mv.add(getMemberValue(clz.getComponentType(), obj, cp));
			}
			if (mv.isEmpty())
				return new ArrayMemberValue(cp);
			ArrayMemberValue am = new ArrayMemberValue(mv.get(0), cp);
			am.setValue(mv.toArray(new MemberValue[0]));
			return am;
		}
		if (clz.isInterface()) {
			return new AnnotationMemberValue(toJavassistAnnotation(clz.getName(), resul, cp), cp);
		}
		EnumMemberValue emv = new EnumMemberValue(cp);
		if (resul != null) {
			emv.setType(((Enum) resul).name());
			emv.setValue(((Enum) resul).toString());
		}
		return emv;
	}

	/**
	 * 将anno注解对象转化为javassist.bytecode.annotation.Annotation 注解
	 * 
	 * @param strClzName
	 *            注解类名
	 * @param an
	 *            注解实例
	 * @param cp
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NotFoundException
	 */
	private static final javassist.bytecode.annotation.Annotation toJavassistAnnotation(String strClzName, Object an,
			ConstPool cp)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NotFoundException {
		javassist.bytecode.annotation.Annotation anno = new javassist.bytecode.annotation.Annotation(strClzName, cp);
		for (Method m : an.getClass().getDeclaredMethods()) {
			if (m.getName().equals("toString") || m.getName().equals("equals") || m.getName().equals("annotationType")
					|| m.getName().equals("hashCode"))
				continue;
			Object resul = m.invoke(an);
			if (resul != null)
				anno.addMemberValue(m.getName(), getMemberValue(m.getReturnType(), resul, cp));
		}
		return anno;
	}

	/**
	 * 向上迭代式查找满足名字的字段
	 * 
	 * @param clz
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unused")
	private static final Field findField(Class<?> clz, String name) {
		return findField(clz, fe -> fe.getName().equals(name));
	}

	/**
	 * 向上迭代式查找满足名字的字段
	 * 
	 * @param clz
	 * @param name
	 * @return
	 */
	private static final Field findField(Class<?> clz, String name, Class<?> type) {
		return findField(clz, fe -> fe.getName().equals(name) && type.equals(fe.getType()));
	}

	/**
	 * 向上迭代式查找满足条件的字段
	 * 
	 * @param clz
	 * @param name
	 * @return
	 */
	private static final Field findField(Class<?> clz, Predicate<Field> predicate) {
		Optional<Field> f;
		do {
			f = Stream.of(clz.getDeclaredFields()).filter(predicate).findFirst();
		} while (!f.isPresent() && (clz = clz.getSuperclass()) != null);
		return f.isPresent() ? f.get() : null;
	}

	/**
	 * 主要用于路径解析
	 * 
	 * @param interfaceClass
	 * @param m
	 * @param actionName
	 * @param pageName
	 * @param firstPath
	 * @return
	 */
	private static final RequestMappingInfo.Builder getRequestMappingInfoBuilder(Class<?> interfaceClass, Method m,
			String actionName, StringBuilder pageName, StringBuilder firstPath) {
		RequestMappingInfo.Builder minfoB = null;

		// set url
		Annotation[] ans = m.getAnnotations();
		for (Annotation a : ans) {
			if (a.annotationType().equals(RequestMapping.class)) {
				RequestMapping rq = (RequestMapping) a;
				String[] url = rq.value();
				if (Objs.isEmpty(url)) {
					url = rq.path();
				}
				minfoB = RequestMappingInfo.paths(url);
				minfoB.paths(url);
				minfoB.consumes(rq.consumes());
				minfoB.headers(rq.headers());
				minfoB.methods(rq.method());
				minfoB.params(rq.params());
				minfoB.produces(rq.produces());
				minfoB.mappingName(rq.name());
			}
			if (a.annotationType().equals(GetMapping.class)) {
				GetMapping rq = (GetMapping) a;
				String[] url = rq.value();
				if (Objs.isEmpty(url)) {
					url = rq.path();
				}
				minfoB = RequestMappingInfo.paths(url);
				minfoB.paths(url);
				minfoB.consumes(rq.consumes());
				minfoB.headers(rq.headers());
				minfoB.methods(RequestMethod.GET);
				minfoB.params(rq.params());
				minfoB.produces(rq.produces());
				minfoB.mappingName(rq.name());
			}
			if (a.annotationType().equals(PostMapping.class)) {
				PostMapping rq = (PostMapping) a;
				String[] url = rq.value();
				if (Objs.isEmpty(url)) {
					url = rq.path();
				}
				minfoB = RequestMappingInfo.paths(url);
				minfoB.paths(url);
				minfoB.consumes(rq.consumes());
				minfoB.headers(rq.headers());
				minfoB.methods(RequestMethod.POST);
				minfoB.params(rq.params());
				minfoB.produces(rq.produces());
				minfoB.mappingName(rq.name());
			}
			if (a.annotationType().equals(DeleteMapping.class)) {
				DeleteMapping rq = (DeleteMapping) a;
				String[] url = rq.value();
				if (Objs.isEmpty(url)) {
					url = rq.path();
				}
				minfoB = RequestMappingInfo.paths(url);
				minfoB.paths(url);
				minfoB.consumes(rq.consumes());
				minfoB.headers(rq.headers());
				minfoB.methods(RequestMethod.DELETE);
				minfoB.params(rq.params());
				minfoB.produces(rq.produces());
				minfoB.mappingName(rq.name());
			}

			if (a.annotationType().equals(PutMapping.class)) {
				PutMapping rq = (PutMapping) a;
				String[] url = rq.value();
				if (Objs.isEmpty(url)) {
					url = rq.path();
				}
				minfoB = RequestMappingInfo.paths(url);
				minfoB.paths(url);
				minfoB.consumes(rq.consumes());
				minfoB.headers(rq.headers());
				minfoB.methods(RequestMethod.PUT);
				minfoB.params(rq.params());
				minfoB.produces(rq.produces());
				minfoB.mappingName(rq.name());
			}
		}

		if (minfoB == null) {
			String[] url;
			// 2018-12-06 暂时只考虑 参数为空
			if (m.getName().equals("execute")) {
				if (!actionName.equals("index"))
					url = new String[] { firstPath.toString(), firstPath + "/" };
				else
					url = new String[] { pageName.toString(), firstPath.toString(), firstPath + "/" };
			} else {
				url = new String[] { firstPath + "/" + m.getName() };
			}
			minfoB = RequestMappingInfo.paths(url);
		}
		return minfoB;
	}

	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException,
			NotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// System.out.println(System.getProperty("java.ext.dirs"));
		// Stream.of(System.getProperty("java.class.path").split(";")).forEach(System.out::println);
		// System.out.println(ServletRequest.class.getResource(""));
		// LoaderClassPath lc = new
		// LoaderClassPath(ServletRequest.class.getClassLoader());
		// System.out.println(ClassPool.getDefault().appendClassPath(SystemConstants.getClassPath().toString()));

		System.out.println(new SrcActionAnalysis().getProxyMethodStr(SrcActionAnalysis.class,
				SrcActionAnalysis.class.getMethod("getter", String.class)));

		// HandleAction ha = new HandleAction(BaseAction.class, null) {
		//
		// @Override
		// public String toString() {
		// super.clz.getName();
		// return super.toString();
		// }
		// };
		//
		// System.out.println(findField(ha.getClass(), "clz"));

		// System.out.println(new RequestMappingHandlerRegisted().toJavassistAnnotation(
		// RequestMappingHandlerRegisted.class.getMethod("getter",
		// String.class).getAnnotations()[0]
		// .annotationType().getName(),
		// RequestMappingHandlerRegisted.class.getMethod("getter",
		// String.class).getAnnotations()[0]));

		// new RequestMappingHandlerRegisted().registerMapping(new
		// RequestMappingHandlerMapping() {
		// @Override
		// protected void registerHandlerMethod(Object handler, Method method,
		// RequestMappingInfo mapping) {
		// super.registerHandlerMethod(handler, method, mapping);
		// }
		//
		// }, PacsAction.class);

	}

	@DeleteMapping({ "delete", "123DEL" })
	@GetMapping("123")
	@RequestMapping(method = RequestMethod.DELETE)
	public Object getter(@RequestParam("123") @Deprecated String str)
			throws NotFoundException, ClassNotFoundException, NoSuchMethodException, SecurityException {
		return null;
	}

}
