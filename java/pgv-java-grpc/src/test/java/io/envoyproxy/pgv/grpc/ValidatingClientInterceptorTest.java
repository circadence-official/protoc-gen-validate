package io.circadence-official.pgv.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.BadRequest;
import com.google.rpc.Status;
import io.circadence-official.pgv.ReflectiveValidatorIndex;
import io.circadence-official.pgv.ValidationException;
import io.circadence-official.pgv.Validator;
import io.circadence-official.pgv.ValidatorIndex;
import io.circadence-official.pgv.grpc.asubpackage.GreeterGrpc;
import io.circadence-official.pgv.grpc.asubpackage.HelloJKRequest;
import io.circadence-official.pgv.grpc.asubpackage.HelloResponse;
import io.grpc.BindableService;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcServerRule;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ValidatingClientInterceptorTest {
    @Rule
    public GrpcServerRule serverRule = new GrpcServerRule();

    private BindableService svc = new GreeterGrpc.GreeterImplBase() {
        @Override
        public void sayHello(HelloJKRequest request, StreamObserver<HelloResponse> responseObserver) {
            responseObserver.onNext(HelloResponse.newBuilder().setMessage("Hello " + request.getName()).build());
            responseObserver.onCompleted();
        }
    };

    @Test
    public void InterceptorPassesValidMessages() {
        serverRule.getServiceRegistry().addService(svc);

        ValidatingClientInterceptor interceptor = new ValidatingClientInterceptor(ValidatorIndex.ALWAYS_VALID);

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverRule.getChannel()).withInterceptors(interceptor);
        stub.sayHello(HelloJKRequest.newBuilder().setName("World").build());
    }

    @Test
    public void InterceptorPassesValidMessagesGenerated() {
        serverRule.getServiceRegistry().addService(svc);

        ValidatingClientInterceptor interceptor = new ValidatingClientInterceptor(new ReflectiveValidatorIndex());

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverRule.getChannel()).withInterceptors(interceptor);
        stub.sayHello(HelloJKRequest.newBuilder().setName("World").build());
    }

    @Test
    public void InterceptorRejectsInvalidMessages() {
        // Don't set up server, so it will error if the call goes through

        ValidatingClientInterceptor interceptor = new ValidatingClientInterceptor(new ValidatorIndex() {
            @Override
            public <T> Validator<T> validatorFor(Class clazz) {
                return proto -> {
                    throw new ValidationException("one", "", "is invalid");
                };
            }
        });

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverRule.getChannel()).withInterceptors(interceptor);
        assertThatThrownBy(() -> stub.sayHello(HelloJKRequest.newBuilder().setName("Foo").build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessage("INVALID_ARGUMENT: one: is invalid - Got ");
    }

    @Test
    public void InterceptorRejectsInvalidMessagesGenerated() {
        // Don't set up server, so it will error if the call goes through
        ValidatingClientInterceptor interceptor = new ValidatingClientInterceptor(new ReflectiveValidatorIndex());

        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(serverRule.getChannel()).withInterceptors(interceptor);

        assertThatExceptionOfType(StatusRuntimeException.class).isThrownBy(() -> stub.sayHello(HelloJKRequest.newBuilder().setName("Foo").build()))
                .withMessageStartingWith("INVALID_ARGUMENT: .io.circadence-official.pgv.grpc.HelloJKRequest.name: must equal World")
                .has(new Condition<>(e -> {
                    try {
                        Status status = StatusProto.fromThrowable(e);
                        Any any = status.getDetailsList().get(0);
                        BadRequest badRequest = any.unpack(BadRequest.class);
                        return badRequest.getFieldViolationsCount() == 1 && badRequest.getFieldViolations(0).getField().equals(".io.circadence-official.pgv.grpc.HelloJKRequest.name")
                                && badRequest.getFieldViolations(0).getDescription().equals("must equal World");
                    } catch (InvalidProtocolBufferException ex) {
                        return false;
                    }
                }, "BadRequest details"));
    }

    // Also testing compilation of proto files with strings of uppercase characters in their filename.
    @Test
    public void InterceptorRejectsInvalidMessagesGenerated2() {
        // Don't set up server, so it will error if the call goes through

        ValidatingClientInterceptor interceptor = new ValidatingClientInterceptor(new ReflectiveValidatorIndex());

        DismisserGrpc.DismisserBlockingStub stub = DismisserGrpc.newBlockingStub(serverRule.getChannel()).withInterceptors(interceptor);

        assertThatExceptionOfType(StatusRuntimeException.class).isThrownBy(() -> stub.sayGoodbye(GooDBYe.GoodbyeJKRequest.newBuilder().setName("Foo").build()))
                .withMessageStartingWith("INVALID_ARGUMENT: .io.circadence-official.pgv.grpc.GoodbyeJKRequest.name: must equal World")
                .has(new Condition<>(e -> {
                    try {
                        Status status = StatusProto.fromThrowable(e);
                        Any any = status.getDetailsList().get(0);
                        BadRequest badRequest = any.unpack(BadRequest.class);
                        return badRequest.getFieldViolationsCount() == 1 && badRequest.getFieldViolations(0).getField().equals(".io.circadence-official.pgv.grpc.GoodbyeJKRequest.name")
                                && badRequest.getFieldViolations(0).getDescription().equals("must equal World");
                    } catch (InvalidProtocolBufferException ex) {
                        return false;
                    }
                }, "BadRequest details"));
    }
}
