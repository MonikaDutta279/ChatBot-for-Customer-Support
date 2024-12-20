
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
//import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class NLPProcessor {
    private StanfordCoreNLP pipeline;

    public NLPProcessor() {
        // Set up the properties for Stanford NLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Processes the input text and returns structured data with recognized entities and processed text.
     *
     * @param text The input text to process
     * @return A result object containing processed text and extracted entities
     */
    public NLPResult processText(String text) {
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);

        StringBuilder processedText = new StringBuilder();
        Map<String, List<String>> entities = new HashMap<>();

        for (CoreLabel token : doc.tokens()) {
            String word = token.word();
            String nerTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

            // Append word to processed text
            processedText.append(word).append(" ");

            // Collect recognized entities
            if (nerTag != null && !nerTag.equals("O")) {  // "O" means no entity
                entities.computeIfAbsent(nerTag, k -> new ArrayList<>()).add(word);
            }
        }

        // Return a structured result
        return new NLPResult(processedText.toString().toLowerCase().trim(), entities);
    }

    /**
     * Inner class to represent the result of text processing.
     */
    public static class NLPResult {
        private final String processedText;
        private final Map<String, List<String>> entities;

        public NLPResult(String processedText, Map<String, List<String>> entities) {
            this.processedText = processedText;
            this.entities = entities;
        }

        public String getProcessedText() {
            return processedText;
        }

        public Map<String, List<String>> getEntities() {
            return entities;
        }

        /**
         * Ensures processedText is returned in lowercase for compatibility.
         */
        public String toLowerCase() {
            return processedText.toLowerCase();
        }

        @Override
        public String toString() {
            return "NLPResult{" +
                   "processedText='" + processedText + '\'' +
                   ", entities=" + entities +
                   '}';
        }
    }
}
