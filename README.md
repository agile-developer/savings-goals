##### Description:
This program is implemented as a simple command-line client to Starling's API. It consists of a '_main()_' that requires
an access-token and a number of days to go back, from today, to gather transactions and calculate a round-up value.
It presents the user with some details of the account (UID, current balance) and also the round-up value calculated for
all **eligible** transactions, for the given time-period. The user is presented with existing savings-goals associated
with their account and given the option to choose one, to transfer the round-up amount to. If the transfer is successful,
the program prints the updated savings-goal and exits. If the user chooses a value other than the options displayed, the
program prints an appropriate message and also exits in this case.

Basically, a single interaction of the program is good for one potential round-up and transfer.

##### Assumptions
* Only transactions having `direction : OUT, status : SETTLED, source : !INTERNAL_TRANSFER` are considered for round-up.
* Transactions from **today** are not included in the round-up, as transactions till _before midnight today_ are considered.
* The same round-up can be transferred multiple times, by running the program with the same transaction date range.
* The program assumes that there are existing savings-goals. It currently doesn't allow adding new savings-goals, so if
there are no existing savings-goals, the program simply exits.

##### Instructions:
* This project can be run with Java SE 8 or upwards, and Gradle 4.8 or upwards.
* Run `./gradlew clean shadowJar` to build the project. 
This will create an executable `savings-goals.jar` file in the `build/libs` directory.
* To run the application issue the following command:  
`java -jar build/libs/savings-goals.jar <valid access-token> <number of days to go back from today>`


##### Improvements:
* Error/Exception handling is pretty basic and can be made more robust.
* By persisting round-up details, duplicate runs of the same transactions can be prevented.
* Currently, all savings-goals for an account are displayed. It would be better to only select goals with enough of a
target remaining.
* Allow creation of new savings-goals from the program.
