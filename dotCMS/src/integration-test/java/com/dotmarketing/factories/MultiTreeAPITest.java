package com.dotmarketing.factories;

import static com.dotcms.util.CollectionsUtils.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dotcms.IntegrationTestBase;
import com.dotcms.datagen.ContainerDataGen;
import com.dotcms.datagen.ContentletDataGen;
import com.dotcms.datagen.FolderDataGen;
import com.dotcms.datagen.HTMLPageDataGen;
import com.dotcms.datagen.StructureDataGen;
import com.dotcms.datagen.TemplateDataGen;
import com.dotcms.util.IntegrationTestInitService;
import com.dotmarketing.beans.MultiTree;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.common.db.DotConnect;
import com.dotmarketing.portlets.containers.model.Container;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.portlets.folders.model.Folder;
import com.dotmarketing.portlets.htmlpageasset.model.HTMLPageAsset;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.portlets.templates.model.Template;
import com.dotmarketing.startup.runonce.Task04315UpdateMultiTreePK;
import com.google.common.collect.Table;

public class MultiTreeAPITest extends IntegrationTestBase {
    

    private static final String CONTAINER = "CONTAINER";
    private static final String PAGE = "PAGE";
    private static final String CONTENTLET = "CONTENTLET";
    private static final String RELATION_TYPE = "RELATION_TYPE";

    final static int runs =2;
    final static int contentlets =5;
    @BeforeClass
    public static void initData() throws Exception {
        IntegrationTestInitService.getInstance().init();
      //  testUpgradeTask();
        buildInitalData();
    }
    
    public static void testUpgradeTask() throws Exception {
        Task04315UpdateMultiTreePK task = Task04315UpdateMultiTreePK.class.newInstance();
        task.executeUpgrade();
    }
    
    
    public static void buildInitalData() throws Exception {
        for(int i=0;i<runs;i++) {
            for(int j=0;j<contentlets;j++) {
                MultiTree mt = new MultiTree()
                        .setContainer(CONTAINER +i)
                        .setHtmlPage(PAGE)
                        .setContentlet(CONTENTLET + j)
                        .setTreeOrder(j)
                        .setInstanceId(RELATION_TYPE + i);

                APILocator.getMultiTreeAPI().saveMultiTree(mt);
            }
        }

    }
    
    @Test
    public  void testDeletes() throws Exception {
        deleteInitialData();
        buildInitalData() ;
        List<MultiTree> all = APILocator.getMultiTreeAPI().getAllMultiTrees();
        
        List<MultiTree> list = APILocator.getMultiTreeAPI().getMultiTrees(PAGE);

        deleteInitialData();
        assertTrue("multiTree deletes", APILocator.getMultiTreeAPI().getAllMultiTrees().size() < all.size() );
        assertTrue("multiTree deletes", APILocator.getMultiTreeAPI().getAllMultiTrees().size() == all.size() - list.size() );
    }
    
    
    @Test
    public  void testReorder() throws Exception {
        deleteInitialData();
        buildInitalData() ;
        MultiTree tree = APILocator.getMultiTreeAPI().getMultiTree(PAGE, CONTAINER+0, CONTENTLET +0, RELATION_TYPE+0);
        assertTrue("multiTree reorders", tree.getTreeOrder()==0 );
        APILocator.getMultiTreeAPI().saveMultiTree(tree.setTreeOrder(7));
        tree = APILocator.getMultiTreeAPI().getMultiTree(PAGE, CONTAINER+ 0, CONTENTLET + 0, RELATION_TYPE+0);
        assertTrue("multiTree reorders", tree.getTreeOrder()==4 );
        APILocator.getMultiTreeAPI().saveMultiTree(tree.setTreeOrder(2));
        List<MultiTree> list = APILocator.getMultiTreeAPI().getMultiTrees(PAGE, CONTAINER+0, RELATION_TYPE+0);
        assertTrue("multiTree reorders", list.get(2).equals(tree));

    }
    
    @Test
    public  void findByChild() throws Exception {
        deleteInitialData();
        buildInitalData() ;
        
        List<MultiTree> list = APILocator.getMultiTreeAPI().getMultiTreesByChild(CONTENTLET + "0");
        
        assertTrue("getByChild returns all results", list.size() == runs );
        
        
        
    }
    
    
    
    @AfterClass
    public static void deleteInitialData() throws Exception {

        List<MultiTree> list = APILocator.getMultiTreeAPI().getMultiTrees(PAGE);

        for(MultiTree tree : list) {
            APILocator.getMultiTreeAPI().deleteMultiTree(tree);
        }

    }
    
    
    
    
    
    @Test
    public  void testSaveMultiTree() throws Exception {
        MultiTree mt = new MultiTree()
                .setContainer(CONTAINER)
                .setHtmlPage(PAGE)
                .setContentlet("NEW_ONE")
                .setTreeOrder(0)
                .setInstanceId(RELATION_TYPE + 0);
        
        APILocator.getMultiTreeAPI().saveMultiTree(mt);
        
        MultiTree mt2 = APILocator.getMultiTreeAPI().getMultiTree(mt.getHtmlPage(), mt.getContainer(), mt.getContentlet(), mt.getRelationType());
        assertTrue("multiTree save and get equals", mt.equals(mt2));
    }
    
    
    

    
    
    
    @Test
    public void testLegacyMultiTreeSave() throws Exception {

        
        long time = System.currentTimeMillis();

        
        MultiTree multiTree = new MultiTree();
        multiTree.setHtmlPage( PAGE+time);
        multiTree.setContainer( CONTAINER +time);
        multiTree.setContentlet( CONTENTLET +time);
        multiTree.setTreeOrder( 1 );
        APILocator.getMultiTreeAPI().saveMultiTree( multiTree );
        
        
        MultiTree mt2 = APILocator.getMultiTreeAPI().getMultiTree(PAGE+time, CONTAINER +time, CONTENTLET +time, Container.LEGACY_RELATION_TYPE);
        
        assertTrue("multiTree save without relationtype and get equals", multiTree.equals(mt2));
    }
    
    
    @Test
    public void testGetPageMultiTrees() throws Exception {


        final Template template = new TemplateDataGen().nextPersisted();
        final Folder folder = new FolderDataGen().nextPersisted();
        final HTMLPageAsset page = new HTMLPageDataGen(folder, template).nextPersisted();
        final Structure structure = new StructureDataGen().nextPersisted();
        final Container container = new ContainerDataGen().withStructure(structure, "").nextPersisted();
        final Contentlet content = new ContentletDataGen(structure.getInode()).nextPersisted();

        
        try {
            MultiTree multiTree = new MultiTree();
            multiTree.setHtmlPage(page);
            multiTree.setContainer(container);
            multiTree.setContentlet(content);
            multiTree.setInstanceId("abc");
            multiTree.setTreeOrder( 1 );
            
            //delete out any previous relation
            APILocator.getMultiTreeAPI().deleteMultiTree(multiTree);
            CacheLocator.getMultiTreeCache().clearCache();
            Table<String, String, Set<String>> trees= APILocator.getMultiTreeAPI().getPageMultiTrees(page, false);
            
            Table<String, String, Set<String>> cachedTrees= APILocator.getMultiTreeAPI().getPageMultiTrees(page, false);
            
            // should be the same object coming from in memory cache
            assert(trees==cachedTrees);
            
            CacheLocator.getMultiTreeCache().removePageMultiTrees(page.getIdentifier());
            
            
            trees= APILocator.getMultiTreeAPI().getPageMultiTrees(page, false);
            
            // cache flush forced a cache reload, so different objects in memory
            assert(trees!=cachedTrees);
            
            // but the objects should contain the same data
            assert(trees.equals(cachedTrees));
    
            // there is no container entry 
            assert(!(cachedTrees.rowKeySet().contains(container.getIdentifier())));
    
    
            // check cache flush on save
            APILocator.getMultiTreeAPI().saveMultiTree( multiTree );
            Table<String, String, Set<String>> addedTrees= APILocator.getMultiTreeAPI().getPageMultiTrees(page, false);
            assert(cachedTrees!=addedTrees);
            
            // did we get a new object from the cache?
            assert(!(cachedTrees.equals(addedTrees)));
            assert(addedTrees.rowKeySet().contains(container.getIdentifier()));
            
            // check cache flush on delete
            APILocator.getMultiTreeAPI().deleteMultiTree(multiTree );
            Table<String, String, Set<String>> deletedTrees= APILocator.getMultiTreeAPI().getPageMultiTrees(page, false);
            
            // did we get a new object from the cache?
            assert(!(addedTrees.equals(deletedTrees)));
            assert(!(deletedTrees.rowKeySet().contains(container.getIdentifier())));
            
        }
        finally {
            ContentletDataGen.remove(content);
            ContainerDataGen.remove(container);
            StructureDataGen.remove(structure);
            HTMLPageDataGen.remove(page);
            FolderDataGen.remove(folder);
            TemplateDataGen.remove(template);
        }

        
        
        
    }



    @Test
    public void testMultiTreeForContainerStructure() throws Exception {

        //THIS USES THE INDEX WHICH IS SOMETIMES NOT THERE LOCALLY
        //final Contentlet contentlet = APILocator.getContentletAPIImpl().findAllContent(0,1).get(0);
        
        Map<String, Object> map = new DotConnect().setSQL("select * from contentlet_version_info").setMaxRows(1).loadObjectResults().get(0);
        final Contentlet contentlet = APILocator.getContentletAPIImpl().find(map.get("working_inode").toString(), APILocator.systemUser(), false);
        
        

        //Create a MultiTree and relate it to that Contentlet
        MultiTree mt = new MultiTree()
                .setContainer(CONTAINER)
                .setHtmlPage(PAGE)
                .setContentlet(contentlet.getIdentifier())
                .setTreeOrder(1)
                .setInstanceId(RELATION_TYPE);
        APILocator.getMultiTreeAPI().saveMultiTree( mt );


        //Search multitrees for the Contentlet, verify its not empty
        List<MultiTree> multiTrees = APILocator.getMultiTreeAPI().getContainerStructureMultiTree(CONTAINER, contentlet.getStructureInode());
        assertNotNull(multiTrees);
        assertFalse(multiTrees.isEmpty());

        //Delete the multitree
        APILocator.getMultiTreeAPI().deleteMultiTree(mt);

        //Search again the relationship should be gone.
        multiTrees = APILocator.getMultiTreeAPI().getContainerStructureMultiTree(CONTAINER, contentlet.getStructureInode());
        assertTrue(multiTrees.isEmpty());

        APILocator.getMultiTreeAPI().deleteMultiTree(mt);
    }



    @Test
    public void testMultiTreesSave() throws Exception {


        long time = System.currentTimeMillis();

        final String parent1 = PAGE + time;

        MultiTree multiTree1 = new MultiTree()
        .setHtmlPage(parent1 )
        .setContainer( CONTAINER +time)
        .setContentlet( CONTENTLET +time)
        .setInstanceId("1")
        .setTreeOrder( 1 );

        long time2 = time + 1;

        MultiTree multiTree2 = new MultiTree()
        .setHtmlPage( parent1 )
        .setContainer( CONTAINER + time2)
        .setContentlet( CONTENTLET + time2)
        .setInstanceId("1")
        .setTreeOrder( 2 );

        APILocator.getMultiTreeAPI().saveMultiTrees( parent1, list(multiTree1, multiTree2) );

        List<MultiTree> multiTrees = APILocator.getMultiTreeAPI().getMultiTrees(parent1);

        assertEquals(2, multiTrees.size());

        MultiTree mtFromDB1 = APILocator.getMultiTreeAPI().getMultiTree(parent1, multiTree1.getContainer(),
                multiTree1.getContentlet(), multiTree1.getRelationType());
        MultiTree mtFromDB2 = APILocator.getMultiTreeAPI().getMultiTree(parent1, multiTree2.getContainer(),
                multiTree2.getContentlet(), multiTree2.getRelationType());


        assertEquals(multiTree1, mtFromDB1);
        assertEquals(multiTree2, mtFromDB2);
    }

    @Test
    public void testMultiTreesSaveWithEmpty() throws Exception {


        long time = System.currentTimeMillis();

        final String parent1 = PAGE + time;

        MultiTree multiTree1 = new MultiTree();
        multiTree1.setHtmlPage( parent1 );
        multiTree1.setContainer( CONTAINER +time);
        multiTree1.setContentlet( CONTENTLET +time);
        multiTree1.setInstanceId("1");
        multiTree1.setTreeOrder( 1 );

        APILocator.getMultiTreeAPI().saveMultiTrees( parent1, list(multiTree1) );
        APILocator.getMultiTreeAPI().saveMultiTrees( parent1, list() );

        List<MultiTree> multiTrees = APILocator.getMultiTreeAPI().getMultiTrees(parent1);

        assertEquals(0, multiTrees.size());
    }

    @Test
    public void testMultiTreesSaveDeleting() throws Exception {


        long time = System.currentTimeMillis();

        final String parent1 = PAGE + time;

        MultiTree multiTree1 = new MultiTree();
        multiTree1.setHtmlPage( parent1 );
        multiTree1.setContainer( CONTAINER +time);
        multiTree1.setContentlet( CONTENTLET +time);
        multiTree1.setInstanceId("1");
        multiTree1.setTreeOrder( 1 );

        long time2 = time + 1;

        MultiTree multiTree2 = new MultiTree();
        multiTree2.setHtmlPage( parent1 );
        multiTree2.setContainer( CONTAINER + time2);
        multiTree2.setContentlet( CONTENTLET + time2);
        multiTree2.setInstanceId("1");
        multiTree2.setTreeOrder( 2 );

        long time3 = time + 2;

        MultiTree multiTree3 = new MultiTree();
        multiTree3.setHtmlPage( parent1 );
        multiTree3.setContainer( CONTAINER + time3);
        multiTree3.setContentlet( CONTENTLET + time3);
        multiTree3.setInstanceId("-1");
        multiTree3.setTreeOrder( 3 );

        APILocator.getMultiTreeAPI().saveMultiTrees( parent1, list(multiTree1, multiTree2, multiTree3) );
        APILocator.getMultiTreeAPI().saveMultiTrees( parent1, list(multiTree1) );

        List<MultiTree> multiTrees = APILocator.getMultiTreeAPI().getMultiTrees(parent1);

        assertEquals(2, multiTrees.size());
        assertTrue(multiTrees.contains(multiTree1));
        assertTrue(multiTrees.contains(multiTree3));
    }
}
