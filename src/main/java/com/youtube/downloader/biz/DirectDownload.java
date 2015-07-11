package com.youtube.downloader.biz;

public class DirectDownload {

//    public static void main(String[] args) {
//        try {
////            // ex: http://www.youtube.com/watch?v=Nj6PFaDmp6c
////            String url = args[0];
////            // ex: "/Users/axet/Downloads"
////            String path = args[1];
//            Search youtubeSearch = new Search();
//            String path = "C:\\Users\\nareshm\\Videos\\Naresh Downloads";
//            List<SearchResult> searchResults = youtubeSearch.find();
//            searchResults.forEach(searchResult -> {
//                String url = createURL(searchResult.getId().getVideoId());
//                System.out.println("Downloading URL:" + url);
//                VGet v = null;
//                try {
//                    v = new VGet(new URL(url), new File(path));
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
//                v.download();
//            });
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private static String createURL(String videoId) {
        return "http://www.youtube.com/watch?v=".concat(videoId);
    }
}