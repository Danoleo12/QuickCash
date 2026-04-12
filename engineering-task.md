# Engineer Task
## User Story 1
### AC 1
1. Given user provided a valid email structure and a valid password, user will be moved to landing page
2. Given user provided an invalid email and a valid password, there will be an error prompting user to re-enter a valid email
### AC 2
1. Given user entered a password without uppercase and a valid email, user will be prompted to include an uppercase letter in their password
2. Given user entered a password without a lowercase and a valid email, user will be prompted to include a lowercase letter in their password
3. Given user entered a password without a special character and a valid email, user will be prompted to include a special character in their password
4. Given user entered a password without a number and a valid email, user will be prompted to include a number in their password
5. Given user entered a password less than 8 characters and a valid email, user will be prompted to have at least 8 characters in their password
6. Given user entered a strong password and a valid email, user will be moved to landing page
### AC 3
1. Given user entered a valid email and a strong password and selected employer, user will be moved to the employer landing page
2. Given user entered a valid email and a strong password and selected employee, user will be moved to the employee landing page
### AC 4
1. Given user entered a valid email and a strong password, we can check for one entry of the user's credential on firebase
2. Given user entered an invalid email or a weak password, we cannot check for one entry of the user's credential on firebase
### AC 5
1. Given user entered a valid email and a strong password and first time registering, we cannot find the user's credential before registering
2. Given user entered a valid email and a strong password and not the first time registering, we can check for one entry of the user's credential on firebase

## User Strory 3
### AC1
1. Given that user entered credentials on the login page, credentials will be compared with the database
2. Given that user entered invalid credentials, user is not loggedIn and error message displayed
3. Given user provided valid credentials, user data will be fetched from the database
4. Given user provided valid credentials, user will be redirected to the corresponding page

### AC2
1. Given user role successfully fetched from database, user redirected to EmployerLandingActivity if role is "employer"
2. Placeholder UI with text "Post a Job" visible on the page

### AC3
1. Given user role successfully fetched from database, user redirected to EmployeeLandingActivity if role is "employee"
2. Placeholder UI with text "Search for Jobs" visible on the page

### AC4
1. Given user just loggedIn, clear back button stack
2. given user just loggendIn, pressing the back button shouldn't take them any where.

## User Story 5
# AC1
1. Logout botton is visible on both "Employer" and "Emplyee" dashboards

# AC2
1. Given logout button is clicked, end firebase stack
2. Given logout button is clicked, end history session

# AC3
1. Given logout button is clicked, end current activity
2. Given logout button is clicked, create intent to new activity
2. Given logout button is clicked, start login activity

# AC4
1. Given back button stack is cleared, user can't return to dashboard