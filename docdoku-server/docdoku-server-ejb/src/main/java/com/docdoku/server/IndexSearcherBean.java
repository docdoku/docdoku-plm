/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server;

/*import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentMasterKey;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import com.docdoku.core.document.DocumentRevisionKey;
import org.apache.lucene.index.CorruptIndexException;
*/

import org.apache.lucene.index.IndexReader;

import javax.annotation.Resource;
import javax.ejb.Stateless;
/*import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
*/
/**
 *
 * @author Florent Garin
 */
@Stateless(name = "IndexSearcherBean")
public class IndexSearcherBean {

    @Resource(name = "indexPath")
    private String indexPath;
    private IndexReader indexReader;
/*
    @PostConstruct
    private void setup() {
        try {
            indexReader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        } catch (CorruptIndexException ex) {
            throw new EJBException(ex);
        } catch (IOException ex) {
            throw new EJBException(ex);
        }
    }

    @PreDestroy
    private void tearDown() {
        try {
            indexReader.close();
        } catch (IOException ex) {
            throw new EJBException(ex);
        }
    }

    public Set<DocumentRevisionKey> searchInIndex(String pWorkspaceId, String pContent) {
        try {
            Set<DocumentRevisionKey> indexedKeys = new HashSet<>();

            Query fullNameQuery = new WildcardQuery(new Term("fullName", pWorkspaceId + "/*"));
            Query contentQuery = new TermQuery(new Term("content", pContent));
            BooleanQuery mainQuery = new BooleanQuery();
            mainQuery.add(fullNameQuery, BooleanClause.Occur.MUST);
            mainQuery.add(contentQuery, BooleanClause.Occur.MUST);

            if (!indexReader.isCurrent()) {
                //TODO use IndexReader.reopen(); when available
                indexReader.close();
                indexReader = IndexReader.open(FSDirectory.open(new File(indexPath)));
            }
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            ScoreDoc[] hits = indexSearcher.search(mainQuery, 500).scoreDocs;
            for (int i = 0; i < hits.length; i++) {
                org.apache.lucene.document.Document doc = indexReader.document(hits[i].doc);
                String fullName = doc.get("fullName");
                String[] partRefs = BinaryResource.parseOwnerRef(fullName).split("/");
                DocumentRevisionKey key = new DocumentRevisionKey(pWorkspaceId, partRefs[0], partRefs[1]);
                indexedKeys.add(key);
            }
   
            return indexedKeys;
        } catch (IOException ex) {
            throw new EJBException(ex);
        }
    }
*/
}
