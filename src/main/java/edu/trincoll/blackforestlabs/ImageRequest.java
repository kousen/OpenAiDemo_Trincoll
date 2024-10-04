package edu.trincoll.blackforestlabs;

// Java record for the request JSON structure
public record ImageRequest(String prompt, int width,
                    int height, boolean promptUpsampling,
                    Integer seed, Integer safetyTolerance) {
    public ImageRequest(String prompt, int width, int height)  {
        this(prompt, width, height, false, null, null);
    }
}
