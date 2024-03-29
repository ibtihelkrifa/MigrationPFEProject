package scala.vermeg.sevices

import java.io.File
import java.util

import com.vermeg.app.models.{BaseCible, BaseSource, RichKey, Transformation}
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class BuildConf {

  val docBuilderFactory = DocumentBuilderFactory.newInstance
  val docBuilder = docBuilderFactory.newDocumentBuilder
  val rollbackdoc= docBuilder.newDocument()
  val racine = rollbackdoc.createElement("Configuration")
  rollbackdoc.appendChild(racine)


  def buildsourceetcible(baseSource:BaseSource, baseCible: BaseCible) =
  {

    var donnédeconnection=rollbackdoc.createElement("DonnéesDeConnection")
    var source =rollbackdoc.createElement("source")
    source.setAttribute("ip",baseSource.getIp)
    source.setAttribute("port",baseSource.getPort)
    source.setAttribute("nomBase",baseSource.getNomBase)
    source.setAttribute("utilisateur",baseSource.getUser)
    source.setAttribute("motdepasse",baseSource.getPassword)
    var cible= rollbackdoc.createElement("cible")
    cible.setAttribute("hbase.master",baseCible.getMaster)
    cible.setAttribute("hbase.zookeeper.quorum",baseCible.getQuorum)
    cible.setAttribute("hbase.zookeeper.property.clientPort",baseCible.getPort)
    donnédeconnection.appendChild(source)
    donnédeconnection.appendChild(cible)
    racine.appendChild(donnédeconnection)



  }

  def setTypeSimulation(typesimulaion: String): Unit =
  {
    racine.setAttribute("typesimulation", typesimulaion)
  }



  def buildTransformations(transformations: java.util.List[Transformation]): Unit =
  {
    var lengthList= transformations.size()
    var i=0
    var transformationsDom=rollbackdoc.createElement("Transformations")
    while(i<lengthList)
      {

        var transformation= rollbackdoc.createElement("Transformation")
        transformationsDom.appendChild(transformation)
        transformation.setAttribute("id",i.toString)
        transformation.setAttribute("typeidLigne",transformations.get(i).getTypeidLigne)
        transformation.setAttribute("idLigne",transformations.get(i).getIdLigne)
        transformation.setAttribute("tablesource",transformations.get(i).getTablesource)
        transformation.setAttribute("tablecible",transformations.get(i).getTablecible)
        var richKeyListLength= transformations.get(i).getRichkeys.size()
        var documentsLength= transformations.get(i).getDocuments.size()
        var richKeyList=transformations.get(i).getRichkeys
        var documentList= transformations.get(i).getDocuments
        var j=0

        while(j< richKeyListLength)
          {
            var richkey= richKeyList.get(j)
            var richKeyDom=rollbackdoc.createElement("CleRicheDeMappage")
            var colonnessources=""
            richkey.getColonnes.forEach(col=>{
              colonnessources=colonnessources+","+col
            })


            richKeyDom.setAttribute("colonnesource",colonnessources.substring(1))
            richKeyDom.setAttribute("type",richkey.getTypecolonnecible)
            if(richkey.getConverter != null)
              {
            richKeyDom.setAttribute("converter",richkey.getConverter)}
            if(richkey.getPattern!=null)
              {
            richKeyDom.setAttribute("pattern",richkey.getPattern)}
            richKeyDom.setAttribute("cartographieformule",richkey.getMappingformula)
            if(richkey.getCondition!=null)
              {
            richKeyDom.setAttribute("condition",richkey.getCondition)}
            richKeyDom.setAttribute("colonnecible",richkey.getColonnecible)
            transformation.appendChild(richKeyDom)
            j=j+1
          }

        var d=0

        while(d < documentsLength)
          {
            var document= documentList.get(d)
            var documentDom= rollbackdoc.createElement("Document")
            var colonnessources=""
            document.getColonnessources.forEach(col=>{
              colonnessources=colonnessources+","+col
            })
            documentDom.setAttribute("colonnessources",colonnessources.substring(1))
            documentDom.setAttribute("clejointure",document.clejointure)
            documentDom.setAttribute("typejointure",document.typejointure)
            documentDom.setAttribute("tablesource",document.tablesource)
            documentDom.setAttribute("colonnescibles",document.colonnecible)
            transformation.appendChild(documentDom)
            d=d+1
          }


        i=i+1
      }
    racine.appendChild(transformationsDom)

    val transformerFactory = TransformerFactory.newInstance
    val transformer = transformerFactory.newTransformer
    val sources = new DOMSource(rollbackdoc)
    val resultat = new StreamResult(new File("testconf2.xml"))
    transformer.transform(sources, resultat)
  }



}
