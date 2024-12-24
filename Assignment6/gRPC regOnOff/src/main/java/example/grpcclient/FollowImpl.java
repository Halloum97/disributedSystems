package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FollowImpl extends FollowGrpc.FollowImplBase {
    private final Map<String, Set<String>> userFollows = new HashMap<>();

    @Override
    public void addUser(UserReq request, StreamObserver<UserRes> responseObserver) {
        String name = request.getName();
        if (userFollows.containsKey(name)) {
            UserRes response = UserRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("User already exists")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        userFollows.put(name, new HashSet<>());

        UserRes response = UserRes.newBuilder()
                .setIsSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void follow(UserReq request, StreamObserver<UserRes> responseObserver) {
        String name = request.getName();
        String followName = request.getFollowName();

        if (!userFollows.containsKey(name) || !userFollows.containsKey(followName)) {
            UserRes response = UserRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("User or follow target does not exist")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        userFollows.get(name).add(followName);

        UserRes response = UserRes.newBuilder()
                .setIsSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void viewFollowing(UserReq request, StreamObserver<UserRes> responseObserver) {
        String name = request.getName();
        if (!userFollows.containsKey(name)) {
            UserRes response = UserRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("User does not exist")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        UserRes response = UserRes.newBuilder()
                .setIsSuccess(true)
                .addAllFollowedUser(userFollows.get(name))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}