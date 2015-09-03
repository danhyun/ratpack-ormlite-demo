Quick ORMLITE demo in Ratpack
-----------------------------

Invoke `./gradlew run` to run app

Sample interactions

```
danny@ARK ~
$ curl -X POST localhost:5050/account/dan
Account dan created
danny@ARK ~
$ curl -X POST localhost:5050/account/jp
Account jp created
danny@ARK ~
$ curl localhost:5050/account/dan
{"name":"dan","password":null}
danny@ARK ~
$ curl localhost:5050/account/jp
{"name":"jp","password":null}
danny@ARK ~
$ curl localhost:5050/account
[{"name":"dan","password":null},{"name":"jp","password":null}]
```