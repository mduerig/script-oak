package michid.script.oak.filestore

import java.util.Date

import org.apache.jackrabbit.oak.tooling.filestore.RecordId

/**
  * Revision from a journal file
  */
case class JournalEntry(rootId: RecordId, timestamp: Date)

object JournalEntry {
  def apply(journalEntry: org.apache.jackrabbit.oak.tooling.filestore.JournalEntry): JournalEntry =
    JournalEntry(journalEntry.id, new Date(journalEntry.timestamp))
}
