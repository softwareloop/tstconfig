Tstconfig description
====================

Tstconfig is system administration tool that tests (a large number of) 
configuration files automatically based on your definitions.

The usage scenario is the following. Suppose you need to install a new
LAMP system. You have to install certain packages, configure certain files,
activate/deactivate certain services or modules. That's a lot of stuff to do.
Steps are easily forgotten and errors can easily introduced.

Also, when the system has been set up and has been running for a while, you
may want to check its integrity, e.g. that configurations are still ok and that
nobody has accidentally broken anything.

This is a typical configuration management problem.

The approach Tstconfig takes is not to change the system, but rather to test
the system's integrity. You still have to make the system modifications for
yourself but you can trust Tstconfig to test that you've actually done 
everything right.

For example, rather than setting up the firewall rules, Tstconfig tests that
the rules are actually they are supposed to be. Rather than activating apache
modules, it test that only the required modules are active.

This makes Tstconfig a great aid for the initial system setup, but also a 
QA assistant that keeps an eye on the system's integrity for you.
