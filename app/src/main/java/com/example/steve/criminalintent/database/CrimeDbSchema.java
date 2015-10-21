package com.example.steve.criminalintent.database;

/**
 * Created by Steve on 10/11/2015.
 * Model level, used to organize all my database-related code
 */
public class CrimeDbSchema {

    //exists to define the String constants needed to describe the moving pieces of my table definition
    //first piece ot that definition is name of the table in my database: CrimeTable.NAME
    public static final class CrimeTable{
        public static final String NAME = "crimes";

        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
            public static final String CONTACT_ID = "contact_id";
        }
    }

}
