# Tstconfig description

Tstconfig is a system administration tool that tests (a large number of)
configuration files automatically. Tstconfig looks at the configurations, checks
that certain properties have the value that you expect, and reports 
any discrepancies.

The main use for Tstconfig is to test that a Linux system is configured in
a secure way in areas such as the firewall (ufw), fail2ban, sshd, apache,
mysql, php, the system's users, etc...

### Running
Tstconfig runs from the command line with the following syntax:

    tstconfig [definition_file]...

### Example definition

In `examples/sshd.tstconfig`:

    ################################################################################
    # Test sshd configuration
    ################################################################################
    
    # The location of the configuration file
    file /etc/ssh/sshd_config
    
    # The syntax for parsing
    parse_mode tokenized
    hash_comment_allowed true
    
    # Check that root cannot login via ssh
    property PermitRootLogin
    assert_eq no
    
    # Check that only certain users can login via ssh
    property AllowUsers
    assert_eq your_user_name

### Output

Tstconfig's output is a report of the tests that passed or failed, with details
on the tests that failed:

    $ tstconfig examples/sshd.tstconfig
    Tstconfig 0.3
    
    Reading definition file: examples/sshd.tstconfig
    ASSERTION FAILED
     File:      /etc/ssh/sshd_config
     Property:  PermitRootLogin
     Value:     yes
     Assertion: assert_eq no
    
    ASSERTION FAILED
     File:      /etc/ssh/sshd_config
     Property:  AllowUsers
     Value:     <undefined>
     Assertion: assert_eq your_user_name
    
    
    SUMMARY REPORT: FAIL
    Assertions tested: 2
    Assertions passed: 0
    Assertions failed: 2
    Errors: 0



### Documentation

* [Tstconfig introduction](https://github.com/softwareloop/tstconfig/wiki)
* [Installing and running](https://github.com/softwareloop/tstconfig/wiki/Installing-and-running)
* [Example: ufw](https://github.com/softwareloop/tstconfig/wiki/Example-for-ufw)
* [Example: fail2ban](https://github.com/softwareloop/tstconfig/wiki/Example-for-fail2ban)
* [Example: /etc/passwd /etc/shadow and /etc/group](https://github.com/softwareloop/tstconfig/wiki/Example-for-etcpasswd-etcshadow-and-etcgroup)
* [Example: netstat](https://github.com/softwareloop/tstconfig/wiki/Example-for-netstat)
