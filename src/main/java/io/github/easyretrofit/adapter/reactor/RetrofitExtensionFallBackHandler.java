//package io.github.easyretrofit.adapter.reactor;
//
//import io.github.easyretrofit.core.CDIBeanManager;
//import io.github.easyretrofit.core.delegate.BaseExceptionDelegate;
//import io.github.easyretrofit.core.delegate.ExceptionDelegateSetGenerator;
//import io.github.easyretrofit.core.delegate.ExceptionDelegator;
//import io.github.easyretrofit.core.exception.RetrofitExtensionException;
//import io.github.easyretrofit.core.extension.InterceptorUtils;
//import io.github.easyretrofit.core.resource.RetrofitApiInterfaceBean;
//import okhttp3.Request;
//import reactor.core.publisher.FluxSink;
//import retrofit2.Call;
//import retrofit2.Response;
//
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.Set;
//import java.util.function.Function;
//
//
//public class RetrofitExtensionFallBackHandler {
//    private final CDIBeanManager cdiBeanManager;
//
//    public RetrofitExtensionFallBackHandler(CDIBeanManager cdiBeanManager) {
//        this.cdiBeanManager = cdiBeanManager;
//    }
//
//    public <T> void handleFailure(FluxSink<Response<T>> sink, Call<T> call, Throwable throwable) {
//        Throwable[] suppressed = throwable.getSuppressed();
//        if (Arrays.stream(suppressed).anyMatch(e -> e instanceof RetrofitExtensionException)) {
//            RetrofitExtensionException exception = (RetrofitExtensionException) Arrays.stream(suppressed).filter(e -> e instanceof RetrofitExtensionException).findFirst().get();
//            Request request = call.request();
//            Method requestMethod = InterceptorUtils.getRequestMethod(request);
//            Class<?> classByRequest = InterceptorUtils.getClassByRequest(request);
//            RetrofitApiInterfaceBean retrofitApiInterfaceBean = exception.getRetrofitApiServiceBean();
//            Function<Class<? extends BaseExceptionDelegate<? extends RetrofitExtensionException>>, BaseExceptionDelegate<? extends RetrofitExtensionException>> function = cdiBeanManager::getBean;
//            Set<BaseExceptionDelegate<? extends RetrofitExtensionException>> exceptionDelegates = ExceptionDelegateSetGenerator.generate(retrofitApiInterfaceBean.getExceptionDelegates(), function);
//            Object exObj = null;
//            for (BaseExceptionDelegate<? extends RetrofitExtensionException> exceptionDelegate : exceptionDelegates) {
//                if (exceptionDelegate.getExceptionClass().isAssignableFrom(exception.getClass())) {
//                    ExceptionDelegator<? extends Throwable> delegator = new ExceptionDelegator<>(exceptionDelegate);
//                    Object invoke = delegator.invoke(null, requestMethod, args, (RetrofitExtensionException) exception);
//                    if (invoke != null) {
//                        exObj = invoke;
//                        break;
//                    }
//                }
//            }
//            return exObj;
//        } else {
//            sink.error(throwable);
//        }
//    }
//}
