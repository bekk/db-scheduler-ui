
## Tekniske muligheter i db-scheduler (ut av boksen)

Forutsetninger: bruker json-serialisering

* Liste executions ("jobber")  (eks. via SchedulerClient, eller rå sql)
  * søke-filter?
* Liste executions i ulike tilstander. Failing/Currently running/Scheduled
* Operasjoner på en execution
    * kjør nå
    * slett
    * vis tilhørende rå json-data hvis json-serialisering er brukt
* Identifisere executions som er
    * long-running
    * failing
    * due (but not started)

## Med tillegg

* Paginering krever sannsynligvis et tilegg i `ScheduledExecutionsFilter` og query, eller at man går rett mot tabellen
* List historiske executions med tredjeparts [logg-modul](https://github.com/rocketbase-io/db-scheduler-log) (egen tabell)
  - alt. er å introdusere et status-felt i datamodellen som kaaan tillate at ferdige executions ligger igjen en stund
    men vil også kreve at man tar vare på en eventuell feil, noe som også dekkes av logg-modulen
* Oppdatere data, men krever nok noe tweaking rundt hvordan serialisering virker, alt. oppdatering rett i tabellen
* ?