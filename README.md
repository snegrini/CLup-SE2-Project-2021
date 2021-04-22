# CLup - Customers Line-up - Software Engineering 2 Project - A.Y. 2020-2021
CLup - Customers Line-up is a Requirement Engineering and Design Project realized for the Software Engineering 2 course at Politecnico di Milano.
Requirement analysis, design choices, implementation and testing are widely described inside the released documents:
- Requirements Analysis and Specifications Document ([RASD1.2.pdf](DeliveryFolder/RASD1.2.pdf));
- Design Document ([DD1.2.pdf](DeliveryFolder/DD1.2.pdf));
- Implementation and Testing Deliverable document ([ITD1.0.pdf](DeliveryFolder/ITD1.0.pdf));
- Acceptance Test Deliverable document ([ATD1.0.pdf](DeliveryFolder/ATD1.0.pdf)).

Artifacts and APKs of the project can be found in the [DeliveryFolder](DeliveryFolder).\
The source code of the implemented part of the project is available in the [Code](Code) folder.

## Structure of the repository
This repository contains all the files that were created in order to design, develop and test the project.
Below are listed alphabetically all the folders with their own content:
- ATD: contains the TeX files used to build the Acceptance Test Deliverable document;
- Assignments: contains the specification of the project to be designed and implemented;
- Code: contains the actual source code of the implemented part of the project;
- DD: contains the TeX files used to build the Design Document;
- DeliveryFolder: contains the delivered documents RASD, DD, ITD and ATD and the artifacts of the server and the client ready to be deployed;
- ITD: contains the TeX files used to build the Implementation and Testing Deliverable document;
- RASD: contains the TeX files used to build the Requirements Analysis and Specifications Document.

## The problem: CLup - Customers Line-up
The coronavirus emergency has put a strain on society on many levels, due to many countries imposing lockdowns that allow people to exit their homes only for essential needs, and enforcing strict rules even when people are justified in going out (such as limiting the number of accesses to buildings and keeping a distance of at least one meter between people).

In particular, grocery shopping---one of the most essential needs---can become a challenge in the presence of such strict rules. Indeed, supermarkets need to restrict access to their stores to avoid having crowds inside, which typically results in long lines forming outside, which are themselves a source of hazards. In these trying times, people turn to technology, and in particular to software applications, to help navigate the challenges created by the imposed restrictions.

### Goal of the project
The goal of this project is to develop an easy-to-use application that, on the one side, allows store managers to regulate the influx of people in the building and, on the other side, saves people from having to line up and stand outside of stores for hours on end.

### Basic feature
The application would work as a digital counterpart to the common situation where people who are in line for a service retrieve a number that gives their position in the queue. Naturally, physically retrieving a number forces people to first approach the building, and then wait in close proximity (though not in a line) until their number is called, which is a less than ideal situation in a lockdown situation. A software application, instead, could provide many improvements to the situation described above.
For example, it would allow customers to "line up" (i.e., retrieve a number) from their home, and then wait until their number is called (or is close to being called) to approach the store. In addition, the application could be used to generate QR codes that would be scanned upon entering the store, thus allowing store managers to monitor entrances. For the application to effectively work in practice, all
customers should use it to access the store, which has a number of consequences, including the following ones:
* The application should be very simple to use, as the range of users include all demographics (everyone needs to do grocery shopping).
* The lining up mechanism should be effective. There is a real risk that the approach does not work in the case the customer arrives to the grocery store after his/her number is called, or too early, as in this case we would get back into a physical line situation. This implies that the system should provide customers with a reasonably precise estimation of the waiting time and should alert them taking into account the time they need to get to the shop from the place they currently are.
* Fallback options should be available for people who do not have access to the required technology; for example, stores should also have the possibility to hand out "tickets" on the spot, thus acting as proxies for the customers.

### Book-a-visit feature
In addition to managing lines in real-time, the application could also allow customers to "book" a visit to the supermarket. This feature would be similar to the booking of a slot for visiting, say, a museum/exhibition, but with important differences. In particular, whereas one can expect that the time that it takes to visit a museum is fairly uniform (and people would typically want to visit the whole museum/exhibition), the same is not true for visits to the supermarket. Hence, upon booking a visit, a customer might indicate also the approximate expected duration of the visit. Alternatively, for long-term customers, this time could be inferred by the system based on an analysis of the previous visits. The application might also allow users to indicate, if not the exact list of items that they intend to purchase, the categories of items that they intend to buy. This would allow the application to plan visits in a finer way, for example allowing more people in the store, if it knows that they are going to buy different things, hence they will occupy different spaces in the store when they visit (thus respecting the requirement that people keep enough distance between them).

### Other features
Other features that the application might have include a suggestion of alternative slots (in the same day, or in different days) for visiting the store, to balance out the number of people in the store, the suggestion of different stores of the same chain (or even of different chains, if the application is chain-independent) if the preferred one is not available, or the periodic notification of available slots in a day/time range (these are other important differences with respect to museums/exhibitions, which are unique, and which are usually visited only once).


## Authors
* [__Samuele Negrini__](https://github.com/snegrini)
* [__Giorgio Piazza__](https://github.com/giorgiopiazza)
