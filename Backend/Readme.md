# 3FA132_Gruppe_3

# Linked projects
https://github.com/Jddk1871/P_3FA132_Gruppe_3_Frontend

# Conventions

- Language: english

# Git

Only commit finished and functional work to main. <br/>
Main has to be functional at all times. <br/>
Releases are based one main.

## Branches

- {type}\_{type_id}\_{user_name}\_{task_description}
    - task_123_jddk_implement_interface

## Commit

- {type_id}\_{user_name}\_{commit_description}
    - #123_jddk_add_interface

### Naming

- Tab: 4 spaces
- Classes & Enums: PascalCase
- Line limit 120 characters
- Methods and Variables: camelCase
- No single letter variables (except temporary)
- private variables start with _
- static variables are in CAPS
- Folder names represent the namespace and start with a capital letter
- Curly brackets start in the next line
- Calling local variables using this.
- No wildcards for library imports


### Endpoint Internal Error Handling 
Internal Errors are always sent back to the requester with code 500.
-> Allowed after consultation with Mr Niedermair 

## Testing
To skip Spring-Server restart in endpoint test set a global system variable to:
````java
SkipServerRestart=True
````

ToDo: 
    - return Customer not only id 
