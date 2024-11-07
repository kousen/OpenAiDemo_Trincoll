package edu.trincoll.recraft;

public class RecraftRecords {
    public record ImageRequest(
            String prompt,
            int n, // num images, must be 1 or 2
            String style, // null, realistic_image, digital_illustration, vector_illustration, icon
            String response_format, // null, url, b64_json
            String size // null, 1024x1024, others
    ) {
    }

    public record ImagesResponse(
            long created,
            Image[] data
    ) {
        public record Image(
                String b64_json,
                String revised_prompt,
                String url
        ) {
        }
    }
}
