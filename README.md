Project Status
--------------

This project has been discontinued, and I will no longer be maintaining it.
Take a look at [HikariCP][2] or [Stormpot][3] instead.


NanoPool
========

[NanoPool][1] is a lightweight and highly scalable JDBC2 connection pool.

By "highly scalable" is meant that NP scales very well (hopefully linearly) to
hundreds of concurrent threads, and with a very low constant-time overhead.

This means that thread contention will be on the *size* of the pool (or the
JDBC driver, or the database, or..) as oppose to the internal implementation
of the connection pool.

License
-------

Apache 2.0

  [1]: http://karmazilla.github.io/nanopool/
  [2]: https://github.com/brettwooldridge/HikariCP
  [3]: https://github.com/chrisvest/stormpot
