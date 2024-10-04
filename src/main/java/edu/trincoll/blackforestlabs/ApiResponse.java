package edu.trincoll.blackforestlabs;

// Java record for the response JSON structure
public record ApiResponse(String id, Status status, Result result) {
    record Result(String sample) {} // The sample is expected to be the URL to the generated image
}
