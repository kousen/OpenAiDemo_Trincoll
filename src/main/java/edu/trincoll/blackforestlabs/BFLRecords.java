package edu.trincoll.blackforestlabs;

public class BFLRecords {
    public record ImageRequest(
            String prompt,
            int width,
            int height,
            boolean promptUpsampling,
            Integer seed,
            Integer safetyTolerance) {

        public ImageRequest(String prompt, int width, int height)  {
            this(prompt, width, height, false, null, 6);
        }

        public ImageRequest(String prompt) {
            this(prompt, 1024, 768, false, null, 6);
        }

        // Validate the request parameters (compact constructor)
        public ImageRequest {
            if (width < 256 || width > 1440) {
                throw new IllegalArgumentException("Width must be between 256 and 1440");
            }
            if (width % 32 != 0) {
                throw new IllegalArgumentException("Width must be a multiple of 32");
            }
            if (height < 256 || height > 1440) {
                throw new IllegalArgumentException("Height must be between 256 and 1440");
            }
            if (height % 32 != 0) {
                throw new IllegalArgumentException("Height must be a multiple of 32");
            }
            if (safetyTolerance != null && (safetyTolerance < 0 || safetyTolerance > 6)) {
                throw new IllegalArgumentException("Safety tolerance must be between 0 and 6");
            }
        }
    }

    public record AsyncResponse(String id) {}

    // Java record for the response JSON structure
    public record ApiResponse(String id, Status status, Result result) {
        record Result(String sample, String prompt) {} // "sample" is the URL to the generated image
    }

    public enum Status {
        Ready, InProgress, TaskNotFound, Failed, Pending, Unknown
    }
}
