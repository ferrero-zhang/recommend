/**
 * 
 */
package com.ifeng.iRecommend.usermodel;

/**
 * <PRE>
 * 作用 : 
 *   新collector，得到所有用户记录；
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 *   
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2013-3-12        likun          create
 * -----------------------------------------------------------------------------
 * </PRE>
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;

public class HitCollector extends Collector {
    private IndexSearcher searcher;
    private LinkedList<ScoreDoc> ll_hits = null;
    ScoreDoc sd;
    int docBase = 0;
    Scorer scorer;
    /** The total number of documents that the collector encountered. */
    public int totalHits;
    
    public HitCollector(IndexSearcher searcher) {
        this.searcher=searcher;
        ll_hits = new LinkedList<ScoreDoc>();
    }
  
	@Override
	public boolean acceptsDocsOutOfOrder() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void collect(int doc)  throws IOException {
		// TODO Auto-generated method stub
		float score = scorer.score();
	    // This collector cannot handle NaN
	    assert !Float.isNaN(score);
	    totalHits++;
	    doc += docBase;
	    sd = new ScoreDoc(doc,score);
	    ll_hits.add(sd);
	}


	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	public LinkedList<ScoreDoc> getScoreDocList() {
		// TODO Auto-generated method stub
		 return ll_hits;
	}

	@Override
	public void setNextReader(AtomicReaderContext context) throws IOException {
		// TODO Auto-generated method stub
		this.docBase = context.docBase;
	}

}