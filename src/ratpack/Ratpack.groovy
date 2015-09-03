import com.google.inject.Inject
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.db.H2DatabaseType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.jdbc.DataSourceConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.DatabaseTable
import com.j256.ormlite.table.TableUtils
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.hikari.HikariModule
import ratpack.server.Service
import ratpack.server.StartEvent

import javax.sql.DataSource

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

@DatabaseTable(tableName = "account")
class Account {
  @DatabaseField(id = true)
  String name
  @DatabaseField
  String password
}

class H2ConnectionDataSource {
  final ConnectionSource connectionSource

  @Inject
  H2ConnectionDataSource(DataSource ds) {
    this.connectionSource = new DataSourceConnectionSource(ds, new H2DatabaseType())
  }
}

class AccountService {
  final Dao<Account, String> accountDao

  @Inject
  AccountService(H2ConnectionDataSource connectionDataSource) {
    this.accountDao = DaoManager.createDao(connectionDataSource.connectionSource, Account)
  }

  Promise<Integer> create(Account account) {
    Blocking.get {
      accountDao.create(account)
    }
  }

  Promise<Account> get(String name) {
    Blocking.get {
      accountDao.queryForId(name)
    }
  }

  Promise<List<Account>> getAll() {
    Blocking.get {
      accountDao.queryForAll()
    }
  }
}

ratpack {
  bindings {
    module MarkupTemplateModule
    module HikariModule, {
      it.addDataSourceProperty("URL", "jdbc:h2:mem:account;INIT=CREATE SCHEMA IF NOT EXISTS DEV")
      it.dataSourceClassName = "org.h2.jdbcx.JdbcDataSource"
    }

    bind H2ConnectionDataSource
    bind AccountService

    bindInstance Service, new Service() {
      @Override
      void onStart(StartEvent event) {
        ConnectionSource connectionSource = event.registry.get(H2ConnectionDataSource).connectionSource
        TableUtils.createTableIfNotExists(connectionSource, Account)
      }
    }
  }

  handlers {

    get('account') { AccountService accountService ->
      accountService.all.then { List<Account> accounts ->
        render json(accounts)
      }
    }

    path('account/:name') { AccountService accountService ->
      byMethod {
        String name = pathTokens.name

        get {
          accountService.get(name).then { Account account ->
            render json(account)
          }
        }

        post {
          Account account = new Account(name: name)
          accountService.create(account).then {
            render "Account $name created"
          }
        }
      }
    }
  }
}
