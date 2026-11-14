
package com.example.storyverse.data.remote.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

public class WikisourceHtmlParser {

    private WikisourceHtmlParser() {
        // Không tạo đối tượng từ class tiện ích này
    }

    /**
     * Trích xuất nội dung tác phẩm từ HTML Wikisource.
     * Chỉ lấy phần div.poem, không lấy thông tin giao diện.
     */
    public static String extractPoemText(String html) {

        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        Document document = Jsoup.parse(html);

        Element poem = document.selectFirst("div.poem");

        if (poem == null) {
            return "";
        }

        // Loại bỏ số thứ tự dòng và thành phần đánh dấu giao diện.
        poem.select(".OptionText").remove();

        StringBuilder result = new StringBuilder();

        NodeTraversor.traverse(new NodeVisitor() {

            @Override
            public void head(Node node, int depth) {

                if (node instanceof TextNode) {
                    result.append(((TextNode) node).getWholeText());
                } else if (node instanceof Element) {

                    Element element = (Element) node;
                    String tag = element.tagName();

                    if ("br".equals(tag)) {
                        result.append('\n');
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {

                if (node instanceof Element) {
                    Element element = (Element) node;

                    if ("p".equals(element.tagName())) {
                        result.append('\n');
                    }
                }
            }

        }, poem);

        return cleanText(result.toString());
    }

    private static String cleanText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace('\u00A0', ' ')
                .replaceAll("[\\t ]+\\n", "\n")
                .replaceAll("\\n[\\t ]+", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
