package com.docdoku.server;

import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IGCMSenderLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESSearcher;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.*;

public class DocumentManagerBeanTest {

    @InjectMocks
    DocumentManagerBean  documentManagerBean = new DocumentManagerBean();

    @Mock
    private EntityManager em;

    @Mock
    private SessionContext ctx;

    @Mock
    private IUserManagerLocal userManager;
    @Mock
    private IMailerLocal mailer;
    @Mock
    private IGCMSenderLocal gcmNotifier;
    @Mock
    private ESIndexer esIndexer;
    @Mock
    private ESSearcher esSearcher;
    @Mock
    private IDataManagerLocal dataManager;

    @Test
    public void saveFileInTemplate() throws Exception {

        documentManagerBean.saveFileInTemplate()
    }

    @Test
    public void saveFileInDocument() throws Exception {

    }
}