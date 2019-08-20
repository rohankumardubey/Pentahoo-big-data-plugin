/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.big.data.kettle.plugins.newhadoopcluster.ui.endpoints;

import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.big.data.kettle.plugins.newhadoopcluster.ui.dialog.NewHadoopClusterDialog;
import org.pentaho.big.data.kettle.plugins.newhadoopcluster.ui.tree.NewHadoopClusterFolderProvider;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.osgi.metastore.locator.api.MetastoreLocator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

import org.pentaho.di.ui.util.HelpUtils;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class NewHadoopClusterEndpoints {

  private static Class<?> PKG = NewHadoopClusterDialog.class;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  private ConnectionManager connectionManager;

  public static final String HELP_URL =
    Const.getDocUrl( BaseMessages.getString( PKG, "ConnectionDialog.help.dialog.Help" ) );

  public NewHadoopClusterEndpoints( MetastoreLocator metastoreLocator ) {
    this.connectionManager = ConnectionManager.getInstance();
    this.connectionManager.setMetastoreSupplier( metastoreLocator::getMetastore );
  }

  @GET
  @Path( "/types" )
  @Produces( { APPLICATION_JSON } )
  public Response getTypes() {
    return Response.ok( connectionManager.getItems() ).build();
  }

  @GET
  @Path( "/new-hadoop-cluster/{scheme}" )
  @Produces( { APPLICATION_JSON } )
  public Response getFields( @PathParam( "scheme" ) String scheme ) {
    return Response.ok( connectionManager.createConnectionDetails( scheme ) ).build();
  }

  @GET
  @Path( "/new-hadoop-cluster" )
  @Consumes( { APPLICATION_JSON } )
  public Response getConnection( @QueryParam( "name" ) String name ) {
    return Response.ok( connectionManager.getConnectionDetails( name ) ).build();
  }

  @GET
  @Path( "/new-hadoop-cluster/exists" )
  @Consumes( { APPLICATION_JSON } )
  public Response getConnectionExists( @QueryParam( "name" ) String name ) {
    return Response.ok( String.valueOf( connectionManager.exists( name ) ) ).build();
  }

  @PUT
  @Path( "/new-hadoop-cluster" )
  @Consumes( { APPLICATION_JSON } )
  public Response createConnection( ConnectionDetails connectionDetails, @QueryParam( "name" ) String name ) {
    boolean saved = connectionManager.save( connectionDetails );
    if ( saved ) {
      connectionManager.delete( name );
      spoonSupplier.get().getShell().getDisplay().asyncExec( () -> spoonSupplier.get().refreshTree(
        NewHadoopClusterFolderProvider.STRING_NEW_HADOOP_CLUSTER ) );
      return Response.ok().build();
    } else {
      return Response.serverError().build();
    }
  }

  @POST
  @Path( "/test" )
  @Consumes( { APPLICATION_JSON } )
  public Response testConnection( ConnectionDetails connectionDetails ) {
    boolean valid = connectionManager.test( connectionDetails );
    if ( valid ) {
      return Response.ok().build();
    } else {
      return Response.status( Response.Status.BAD_REQUEST ).build();
    }
  }

  @GET
  @Path( "/help" )
  public Response help() {
    spoonSupplier.get().getShell().getDisplay().asyncExec( () ->
      HelpUtils.openHelpDialog( spoonSupplier.get().getDisplay().getActiveShell(),
        BaseMessages.getString( PKG, "ConnectionDialog.help.dialog.Title" ),
        HELP_URL, BaseMessages.getString( PKG, "ConnectionDialog.help.dialog.Header" ) ) );
    return Response.ok().build();
  }
}