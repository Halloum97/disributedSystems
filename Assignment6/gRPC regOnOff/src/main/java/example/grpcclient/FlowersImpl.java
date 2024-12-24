package example.grpcclient;

import io.grpc.stub.StreamObserver;
import service.*;
import com.google.protobuf.Empty;

import java.util.HashMap;
import java.util.Map;

public class FlowersImpl extends FlowersGrpc.FlowersImplBase {
    private final Map<String, Flower> flowers = new HashMap<>();

    @Override
    public void plantFlower(FlowerReq request, StreamObserver<FlowerRes> responseObserver) {
        String name = request.getName();
        if (flowers.containsKey(name)) {
            FlowerRes response = FlowerRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("Flower name already taken")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        Flower flower = Flower.newBuilder()
                .setName(name)
                .setWaterTimes(request.getWaterTimes())
                .setBloomTime(request.getBloomTime())
                .setFlowerState(State.PLANTED)
                .build();
        flowers.put(name, flower);

        FlowerRes response = FlowerRes.newBuilder()
                .setIsSuccess(true)
                .setMessage("Flower planted successfully")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void viewFlowers(Empty request, StreamObserver<FlowerViewRes> responseObserver) {
        FlowerViewRes.Builder responseBuilder = FlowerViewRes.newBuilder().setIsSuccess(true);
        responseBuilder.addAllFlowers(flowers.values());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void waterFlower(FlowerCare request, StreamObserver<WaterRes> responseObserver) {
        String name = request.getName();
        Flower flower = flowers.get(name);
        if (flower == null) {
            WaterRes response = WaterRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("Flower not found")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        if (flower.getFlowerState() == State.BLOOMING || flower.getFlowerState() == State.DEAD) {
            WaterRes response = WaterRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("Cannot water a blooming or dead flower")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        flower = flower.toBuilder().setWaterTimes(flower.getWaterTimes() - 1).build();
        if (flower.getWaterTimes() <= 0) {
            flower = flower.toBuilder().setFlowerState(State.BLOOMING).build();
        }
        flowers.put(name, flower);

        WaterRes response = WaterRes.newBuilder()
                .setIsSuccess(true)
                .setMessage("Flower watered successfully")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void careForFlower(FlowerCare request, StreamObserver<CareRes> responseObserver) {
        String name = request.getName();
        Flower flower = flowers.get(name);
        if (flower == null) {
            CareRes response = CareRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("Flower not found")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        if (flower.getFlowerState() != State.BLOOMING) {
            CareRes response = CareRes.newBuilder()
                    .setIsSuccess(false)
                    .setError("Flower is not blooming")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        flower = flower.toBuilder().setBloomTime(flower.getBloomTime() + 1).build();
        flowers.put(name, flower);

        CareRes response = CareRes.newBuilder()
                .setIsSuccess(true)
                .setMessage("Flower cared for successfully")
                .setBloomTime(flower.getBloomTime())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}