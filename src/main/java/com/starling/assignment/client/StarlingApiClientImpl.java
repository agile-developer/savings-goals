package com.starling.assignment.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.starling.assignment.model.Account;
import com.starling.assignment.model.Amount;
import com.starling.assignment.model.Balance;
import com.starling.assignment.model.SavingsGoal;
import com.starling.assignment.model.Transaction;
import com.starling.assignment.service.BankingService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StarlingApiClientImpl implements StarlingApiClient {

    private static final String ACCOUNTS = "/accounts";
    private static final String BALANCE = "/{accountUid}/balance";
    private static final String TRANSACTION_FEED = "/feed/account/{accountUid}/category/{categoryUid}";
    private static final String SAVINGS_GOALS = "/account/{accountUid}/savings-goals";
    private static final String SAVINGS_GOAL = SAVINGS_GOALS + "/{savingsGoalUid}";
    private static final String TRANSFER_TO_SAVINGS_GOAL = SAVINGS_GOAL + "/add-money/{transferUid}";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String STARLING_API_URL;
    private final String accessToken;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public StarlingApiClientImpl(String baseUrl, String apiVersion, String accessToken) {

        this.STARLING_API_URL = baseUrl + apiVersion;
        this.accessToken = accessToken;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    @Override
    public List<Account> getAccounts() {

        Request accountsRequest = requestBuilder(STARLING_API_URL + ACCOUNTS)
            .get()
            .build();

        String accountsJson = getResponseString(accountsRequest);

        JsonObject accountsJsonObject = gson.fromJson(accountsJson, JsonObject.class);
        JsonArray accountsJsonArray = accountsJsonObject.getAsJsonArray("accounts");
        List<Account> accounts = new ArrayList<>();
        accountsJsonArray.forEach(jsonElement -> accounts.add(gson.fromJson(jsonElement, Account.class)));

        return accounts;
    }

    @Override
    public Balance getAccountBalance(String accountUid) {

        String pathParams = BALANCE.replace("{accountUid}", accountUid);
        Request accountBalanceRequest = requestBuilder(STARLING_API_URL + ACCOUNTS + pathParams)
            .get()
            .build();

        String accountBalanceJson = getResponseString(accountBalanceRequest);
        JsonObject accountBalanceJsonObject = gson.fromJson(accountBalanceJson, JsonObject.class);
        JsonElement amountElement = accountBalanceJsonObject.get("amount");

        return new Balance(gson.fromJson(amountElement, Amount.class));
    }

    @Override
    public List<Transaction> getTransactionFeed(String accountUid, String categoryUid) {

        String pathParams = TRANSACTION_FEED.replace("{accountUid}", accountUid)
            .replace("{categoryUid}", categoryUid);
        Request transactionFeedRequest = requestBuilder(STARLING_API_URL + pathParams)
            .get()
            .build();

        String transactionFeedJson = getResponseString(transactionFeedRequest);

        JsonObject transactionFeedJsonObject = gson.fromJson(transactionFeedJson, JsonObject.class);
        JsonArray transactionsJsonArray = transactionFeedJsonObject.getAsJsonArray("feedItems");
        List<Transaction> transactions = new ArrayList<>();
        transactionsJsonArray.forEach(jsonElement -> transactions.add(gson.fromJson(jsonElement, Transaction.class)));

        return transactions;
    }

    @Override
    public List<SavingsGoal> getSavingsGoals(String accountUid) {

        String pathParams = SAVINGS_GOALS.replace("{accountUid}", accountUid);
        Request savingsGoalsRequest = requestBuilder(STARLING_API_URL + pathParams)
            .get()
            .build();

        String savingsGoalsJson = getResponseString(savingsGoalsRequest);
        JsonObject savingsGoalsJsonObject = gson.fromJson(savingsGoalsJson, JsonObject.class);
        JsonArray savingsGoalsJsonArray = savingsGoalsJsonObject.getAsJsonArray("savingsGoalList");
        List<SavingsGoal> savingsGoals = new ArrayList<>();
        savingsGoalsJsonArray.forEach(jsonElement -> savingsGoals.add(gson.fromJson(jsonElement, SavingsGoal.class)));

        return savingsGoals;
    }

    @Override
    public SavingsGoal getSavingsGoal(String accountUid, String savingsGoalUid) {

        String pathParams = SAVINGS_GOAL.replace("{accountUid}", accountUid)
            .replace("{savingsGoalUid}", savingsGoalUid);
        Request savingsGoalsRequest = requestBuilder(STARLING_API_URL + pathParams)
            .get()
            .build();

        String savingsGoalJson = getResponseString(savingsGoalsRequest);

        return gson.fromJson(savingsGoalJson, SavingsGoal.class);
    }

    @Override
    public String createSavingsGoal(String accountUid, BankingService.SavingsGoalRequest savingsGoalRequest) {

        String savingsGoalRequestJson = gson.toJson(savingsGoalRequest);
        RequestBody requestBody = RequestBody.create(JSON, savingsGoalRequestJson);
        String pathParams = SAVINGS_GOALS.replace("{accountUid}", accountUid);
        Request createSavingsGoalRequest = requestBuilder(STARLING_API_URL + pathParams)
            .put(requestBody)
            .build();

        String createSavingsGoalJson = getResponseString(createSavingsGoalRequest);
        JsonObject createSavingsGoalJsonObject = gson.fromJson(createSavingsGoalJson, JsonObject.class);

        return createSavingsGoalJsonObject.get("savingsGoalUid").toString();
    }

    @Override
    public boolean transferMoneyToSavingsGoal(String accountUid, String savingsGoalUid, String transferUid, BankingService.SavingsGoalAmount savingGoalAmount) {

        String savingsGoalAmountJson = gson.toJson(savingGoalAmount);
        RequestBody requestBody = RequestBody.create(JSON, savingsGoalAmountJson);
        String pathParams = TRANSFER_TO_SAVINGS_GOAL.replace("{accountUid}", accountUid)
            .replace("{savingsGoalUid}", savingsGoalUid).replace("{transferUid}", transferUid);
        Request moneyToSavingsGoalRequest = requestBuilder(STARLING_API_URL + pathParams)
            .put(requestBody)
            .build();

        String transferToSavingsGoalJson = getResponseString(moneyToSavingsGoalRequest);
        JsonObject transferToSavingsGoalJsonObject = gson.fromJson(transferToSavingsGoalJson, JsonObject.class);

        return transferToSavingsGoalJsonObject.get("success").getAsBoolean();
    }

    private Request.Builder requestBuilder(String url) {

        return new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + accessToken)
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Agile Developer")
            .addHeader("Content-Type", "application/json");
    }

    private String getResponseString(Request request) {

        String responseString;
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                throw new RuntimeException("No response body received from Starling API.");
            }
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                    String.format("HTTP response code %d received from Starling API.", response.code()));
            }
            responseString = body.string();
        } catch (IOException e) {
            throw new RuntimeException("Encountered IOException calling Starling API: " + e.getMessage());
        }

        return responseString;
    }
}
