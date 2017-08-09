package ele.me.sd.elasticsearch.scripts;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.*;

import java.util.Map;
import java.util.*;

public class MatchModeScoringScriptPlugin extends Plugin implements ScriptPlugin {

    private static final String MATCH_MODE_EXACT = "Exact";

    private static final String MATCH_MODE_CONTAIN = "Contain";

    private static final String MATCH_MODE_SPLIT_CONTAIN = "SplitContain";

    private static final String MATCH_MODE_FUZZY = "Fuzzy";

    public List<NativeScriptFactory> getNativeScripts() {

        List<NativeScriptFactory> list = new ArrayList<NativeScriptFactory>();
        list.add(new MatchModeScoringScriptFactory());
        return list;
    }

    public static class MatchModeScoringScriptFactory implements NativeScriptFactory {
        public ExecutableScript newScript( Map<String, Object> params) {

            return new MatchModeScoringScript(params);
        }
        public boolean needsScores() {
            return false;
        }
        public String getName() {
            return "match_mode_scoring";
        }
    }

    public static class MatchModeScoringScript extends AbstractLongSearchScript {


        private Map<String, Object> params;

        public MatchModeScoringScript(Map<String, Object> params){
            this.params = params;
        }

        public long runAsLong() {

            String query = null;
            String[] tokens = null;
            String bid = source().get("biding").toString();
            Object queryObj = this.params.get("query");
            if(queryObj != null){
                query = queryObj.toString();
            }
            Object tokensObj = this.params.get("tokens");
            if(tokensObj != null){
                tokens = tokensObj.toString().split(";");
            }
            if(query != null && tokens != null){
                Object matchModeObj = source().get("matchMode");
                if(matchModeObj != null){
                    String matchMode = matchModeObj.toString();
                    if(matchMode.equals(MATCH_MODE_EXACT)){

                        return query.equals(bid) ? 1000 : 0;
                    }
                    else if(matchMode.equals(MATCH_MODE_CONTAIN)){

                        return (query.contains(bid) || bid.contains(query)) ? 100 : 0;
                    }
                    else if(matchMode.equals(MATCH_MODE_SPLIT_CONTAIN)){

                        for(String token : tokens){
                            if(token.contains(bid) || bid.contains(token)){
                                return 10;
                            }
                        }
                        return 0;
                    }
                    else if(matchMode.equals(MATCH_MODE_FUZZY)){

                        return 1;
                    }
                }
            }


            return 0;
        }
    }
}